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
import org.theabconline.smsservice.dto.JdyRecordDTO;
import org.theabconline.smsservice.dto.SmsRequestDTO;
import org.theabconline.smsservice.dto.UserRegistrationDTO;
import org.theabconline.smsservice.mapping.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParsingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParsingService.class);

    private static final String DATA_ID_WIDGET = "_id";

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
    public ParsingService(ObjectMapper mapper, FormMappings formMappings) {
        this.mapper = mapper;
        this.formMappings = formMappings;
    }

    @PostConstruct
    private void buildIdFormsMap() {
        for (Form form : formMappings.getForms()) {
            idFormsMap.put(form.getFormId(), form);
        }
    }

    public FormMetadata getFormMetadata(String message) throws IOException {
        String formId = getFormId(message);
        Form form = getForm(formId);
        String metadataWidget = form.getMetadataWidget();
        return mapper.readValue(getFieldValue(message, metadataWidget), FormMetadata.class);
    }

    public String getMessageWidget(String appId, String entryId) {
        Form form = idFormsMap.get(appId + entryId);
        return form.getErrorMessageWidget();
    }

    public String getPhoneNumbers(String message, String phoneNumbersWidget) throws IOException {
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

    public String getPayload(String message, List<FieldMapping> fieldMappings) throws IOException {
        Map<String, String> payloadMap = Maps.newHashMap();
        for (FieldMapping fieldMapping : fieldMappings) {
            String fieldValue = getFieldValue(message, fieldMapping.getWidget());
            payloadMap.put(fieldMapping.getSmsField(), fieldValue);
        }

        return mapper.writeValueAsString(payloadMap);
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

    public String getAppId(String message) throws IOException {
        return getFieldValue(message, appIdFieldName);
    }

    public String getEntryId(String message) throws IOException {
        return getFieldValue(message, entryIdFieldName);
    }

    public String getDataId(String message) throws IOException {
        return getFieldValue(message, DATA_ID_WIDGET);
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
        String fieldValue;

        try {
            JsonNode jsonTree = mapper.readTree(message);
            fieldValue = jsonTree.at(path).get(fieldName).textValue();
        } catch (IOException e) {
            String errorMessage = String.format("Error parsing - path: %s, field name: %s", path, fieldName);
            LOGGER.error("Unable to get field value. path: {}, field name: {}, message: {}", path, fieldName, message);
            throw new IOException(errorMessage);
        }

        return fieldValue;
    }

    private Form getForm(String formId) {
        Form form = idFormsMap.get(formId);

        if (form == null) {
            throw new RuntimeException(String.format("Cannot find formId %s", formId));
        }

        return form;
    }

}
