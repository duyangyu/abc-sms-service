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
import org.theabconline.smsservice.dto.SmsExceptionDTO;
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
            List<String> phoneNumbers = getPhoneNumbers(message, smsTemplate.getPhoneNumbersWidget());
            Map<String, String> params = Maps.newHashMap();
            for (FieldMapping fieldMapping : smsTemplate.getFieldMappings()) {
                String fieldValue = getFieldValue(message, fieldMapping.getWidget());
                params.put(fieldMapping.getSmsField(), fieldValue);
            }
            SmsDTO smsDTO = new SmsDTO(Joiner.on(",").join(phoneNumbers), smsTemplateCode, mapper.writeValueAsString(params));
            smsDTOList.add(smsDTO);
        }
//        List<String> templateCodes = getTemplateCodes(message);
//        List<String> phoneNumbers = getPhoneNumbers(message);
//        List<String> smsParams = getJsonStrings(message);

//        for (int i = 0; i < templateCodes.size(); i++) {
//            String phoneNumber = phoneNumbers.get(i);
//            String templateCode = templateCodes.get(i);
//            String smsParam = smsParams.get(i);
//
//            smsDTOList.add(new SmsDTO(phoneNumber, templateCode, smsParam));
//        }

        return smsDTOList;
    }

    private List<String> getPhoneNumbers(String message, String phoneNumbersWidget) {
        return Lists.newArrayList();
    }

    public UserRegistrationDTO getUserParams(String message) throws IOException {
        UserRegistrationDTO result = new UserRegistrationDTO();
        RegistrationForm registrationForm = formMappings.getRegistrationForm();

        String name = getFieldValue(message, registrationForm.getFieldsPath(), registrationForm.getNameFieldName());
        String email = getFieldValue(message, registrationForm.getFieldsPath(), registrationForm.getEmailFieldName());
        String mobile = getFieldValue(message, registrationForm.getFieldsPath(), registrationForm.getMobileFieldName());

        result.setName(name);
        result.setEmail(email);
        result.setMobile(mobile);

        return result;
    }

//    private List<String> getTemplateCodes(String message) throws IOException {
//        return idFormsMap.get(getFormId(message)).getTemplateCodes();
//    }
//
//    private List<String> getPhoneNumbers(String message) throws IOException {
//        List<String> phoneNumbers = Lists.newArrayList();
//        Form form = getForm(getFormId(message));
//        String phoneNumberPath = form.getPhoneNumberPath();
//        List<String> phoneNumberFieldNames = form.getPhoneNumberFieldNames();
//        for(String phoneNumberFieldName : phoneNumberFieldNames) {
//            phoneNumbers.add(getFieldValue(message, phoneNumberPath, phoneNumberFieldName));
//        }
//
//        return phoneNumbers;
//
//    }
//
//    private List<String> getJsonStrings(String message) throws IOException {
//        List<String> jsonStrings = Lists.newArrayList();
//        Form form = getForm(getFormId(message));
//        List<Recipient> recipients = formMappings.getMappings().get(form.getName()).getRecipients();
//        for (Recipient recipient : recipients) {
//            List<Field> fields = recipient.getFields();
//            String jsonString = assembleJsonString(fields, message);
//            LOGGER.debug("JSON string assembled: {}", jsonString);
//            jsonStrings.add(jsonString);
//        }
//
//        return jsonStrings;
//    }
//
//    private String assembleJsonString(List<Field> fields, String message) throws IOException {
//        Map<String, String> keyValuePairs = Maps.newHashMap();
//
//        for (Field field : fields) {
//            String path = field.getPath();
//            String fieldName = field.getFieldName();
//            String templateKey = field.getTemplateKey();
//            String value = getFieldValue(message, path, fieldName);
//            keyValuePairs.put(templateKey, value);
//        }
//
//        return mapper.writeValueAsString(keyValuePairs);
//    }

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
