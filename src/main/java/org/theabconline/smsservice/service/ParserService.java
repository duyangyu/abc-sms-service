package org.theabconline.smsservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.theabconline.smsservice.dto.SmsDTO;
import org.theabconline.smsservice.dto.UserRegistrationDTO;
import org.theabconline.smsservice.mapping.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParserService.class);

    private ObjectMapper mapper;

    private FormMappings formMappings;

    @Value("${jdyun.formIdPath:/data}")
    private String formIdPath;

    @Value("${jdyun.appIdFieldName:appId}")
    private String appIdFieldName;

    @Value("${jdyun.entryIdFieldName:entryId}")
    private String entryIdFieldName;

    private Map<String, Form> idFormsMap = new HashMap<>();

    @Autowired
    public ParserService(ObjectMapper mapper, FormMappings formMappings) {
        this.mapper = mapper;
        this.formMappings = formMappings;
    }

    @PostConstruct
    private void buildIdFormsMap() {
        for (Form form : formMappings.getForms()) {
            idFormsMap.put(form.getFormId(), form);
        }
    }

    public List<SmsDTO> getSmsParams(String message) throws IOException {
        List<SmsDTO> smsDTOList = Lists.newArrayList();
        String formId = getFormId(message);
        Form form = getForm(formId);
        String metadataWidget = form.getMetadataWidget();
        FormMetadata formMetadata = mapper.readValue(getFieldValue(message, metadataWidget), FormMetadata.class);
        for (SmsTemplate smsTemplate : formMetadata.getSmsTemplates()) {
            String smsTemplateCode = smsTemplate.getSmsTemplateCode();
            String phoneNumbers = getPhoneNumbers(message, smsTemplate.getPhoneNumbersWidget());
            Map<String, String> params = Maps.newHashMap();
            for (FieldMapping fieldMapping : smsTemplate.getFieldMappings()) {
                String fieldValue = getFieldValue(message, fieldMapping.getWidget());
                params.put(fieldMapping.getSmsField(), fieldValue);
            }
            SmsDTO smsDTO = new SmsDTO(phoneNumbers, smsTemplateCode, mapper.writeValueAsString(params));
            smsDTOList.add(smsDTO);
        }

        return smsDTOList;
    }

    private String getPhoneNumbers(String message, String phoneNumbersWidget) throws IOException {
        String result;
        JsonNode phoneNumberField = mapper.readTree(message).at(FormMappings.DEFAULT_PATH).get(phoneNumbersWidget);

        if (phoneNumberField.isArray()) {
            List phoneNumberList = mapper.convertValue(phoneNumberField, List.class);
            result = Joiner.on(",").join(phoneNumberList);
        } else {
            result = getFieldValue(message, phoneNumbersWidget);
        }

        return result;
    }

    public UserRegistrationDTO getUserParams(String message) throws IOException {
        UserRegistrationDTO result = new UserRegistrationDTO();
        RegistrationForm registrationForm = formMappings.getRegistrationForm();

        String name = getFieldValue(message, registrationForm.getNameFieldName());
        String email = getFieldValue(message, registrationForm.getEmailFieldName());
        String mobile = getFieldValue(message, registrationForm.getMobileFieldName());

        result.setName(name);
        result.setEmail(email);
        result.setMobile(mobile);

        return result;
    }

    private String getFormId(String message) throws IOException {
        String entryId = getFieldValue(message, formIdPath, entryIdFieldName);
        String appId = getFieldValue(message, formIdPath, appIdFieldName);
        return appId + entryId;
    }

    private String getFieldValue(String message, String fieldName) throws IOException {
        return getFieldValue(message, FormMappings.DEFAULT_PATH, fieldName);
    }

    private String getFieldValue(String message, String path, String fieldName) throws IOException {
        JsonNode jsonTree = mapper.readTree(message);

        String fieldValue;
        try {
            fieldValue = jsonTree.at(path).get(fieldName).textValue();
        } catch (Exception e) {
            LOGGER.error("Unable to get field value. path: {}, field name: {}, message: {}", path, fieldName, message);
            throw e;
        }

        return fieldValue;
    }

    private Form getForm(String formId) {
        Form form = idFormsMap.get(formId);

        if (form == null) {
            throw new RuntimeException("Invalid form id");
        }

        return form;
    }

}
