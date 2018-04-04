package org.theabconline.smsservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper mapper;

    private final FormMappingProperties formMappingProperties;

    @Value("${jdyun.formIdPath}")
    private String formIdPath;

    @Value("${jdyun.formIdFieldName}")
    private String formIdFieldName;

    private Map<String, String> formsIdNameMap = new HashMap<>();

    @Autowired
    public ParserService(ObjectMapper mapper, FormMappingProperties formMappingProperties) {
        this.mapper = mapper;
        this.formMappingProperties = formMappingProperties;
    }

    public String getParams(String message) throws IOException {
        Map<String, String> smsParams = new HashMap<>();
        List<Field> fields = getFields(message);
        for (Field field : fields) {
            String path = field.getPath();
            String fieldName = field.getFieldName();
            String templateKey = field.getTemplateKey();
            String value = getFieldValue(message, path, fieldName);
            smsParams.put(templateKey, value);
        }

        return mapper.writeValueAsString(smsParams).replace("\"", "\\\"");
    }

    private List<Field> getFields(String message) throws IOException {
        String formId = getFormId(message);
        String formName = getFormName(formId);

        return formMappingProperties.getMappings().get(formName).getFields();
    }

    private String getFormId(String message) throws IOException {
        return getFieldValue(message, formIdPath, formIdFieldName);
    }

    private String getFieldValue(String message, String path, String fieldName) throws IOException {
        JsonNode jsonTree = mapper.readTree(message);

        return jsonTree.at(path).get(fieldName).textValue();
    }

    private String getFormName(String formId) {
        String formName = formsIdNameMap.get(formId);

        if (formName == null) {
            throw new RuntimeException("Invalid form id");
        }

        return formName;
    }

    @PostConstruct
    private void buildFormIdNameMap() {
        for (FormMappingProperties.Form form : formMappingProperties.getForms()) {
            formsIdNameMap.put(form.getId(), form.getName());
        }
    }

}
