package org.theabconline.smsservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.theabconline.smsservice.config.FormMappingProperties;
import org.theabconline.smsservice.config.FormMappingProperties.Field;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParserService.class);

    private ObjectMapper mapper;

    private FormMappingProperties formMappingProperties;

    @Value("${jdyun.formIdPath}")
    private String formIdPath;

    @Value("${jdyun.appIdFieldName}")
    private String appIdFieldName;

    @Value("${jdyun.entryIdFieldName}")
    private String entryIdFieldName;

    private Map<String, FormMappingProperties.Form> idFormsMap = new HashMap<>();

    @Autowired
    public ParserService(ObjectMapper mapper, FormMappingProperties formMappingProperties) {
        this.mapper = mapper;
        this.formMappingProperties = formMappingProperties;
    }

    public SmsVO getSmsParams(String message) throws IOException {
        String templateCode = getTemplateCode(message);
        String phoneNumber = getPhoneNumber(message);
        String smsParams = getParams(message);

        return new SmsVO(phoneNumber, templateCode, smsParams);
    }

    private String getTemplateCode(String message) throws IOException {
        return idFormsMap.get(getFormId(message)).getTemplateCode();
    }

    private String getPhoneNumber(String message) throws IOException {
        FormMappingProperties.Form form = idFormsMap.get(getFormId(message));
        String phoneNumberPath = form.getPhoneNumberPath();
        String phoneNumberFieldName = form.getPhoneNumberFieldName();
        return getFieldValue(message, phoneNumberPath, phoneNumberFieldName);

    }

    private String getParams(String message) throws IOException {
        Map<String, String> smsParamsMap = new HashMap<>();
        List<Field> fields = getFields(message);
        for (Field field : fields) {
            String path = field.getPath();
            String fieldName = field.getFieldName();
            String templateKey = field.getTemplateKey();
            String value = getFieldValue(message, path, fieldName);
            smsParamsMap.put(templateKey, value);
        }

        return mapper.writeValueAsString(smsParamsMap);
    }

    private List<Field> getFields(String message) throws IOException {
        String formId = getFormId(message);
        String formName = getForm(formId).getName();

        return formMappingProperties.getMappings().get(formName).getFields();
    }

    private String getFormId(String message) throws IOException {
        String appId = getFieldValue(message, formIdPath, appIdFieldName);
        String entryId = getFieldValue(message, formIdPath, entryIdFieldName);
        return appId + entryId;
    }

    private String getFieldValue(String message, String path, String fieldName) throws IOException {
        JsonNode jsonTree = mapper.readTree(message);

        return jsonTree.at(path).get(fieldName).textValue();
    }

    private FormMappingProperties.Form getForm(String formId) {
        FormMappingProperties.Form form = idFormsMap.get(formId);

        if (form == null) {
            throw new RuntimeException("Invalid form id");
        }

        return form;
    }

    @PostConstruct
    private void buildIdFormsMap() {
        for (FormMappingProperties.Form form : formMappingProperties.getForms()) {
            idFormsMap.put(form.getFormId(), form);
        }
    }

}
