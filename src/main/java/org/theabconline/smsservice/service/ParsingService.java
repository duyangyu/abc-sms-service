package org.theabconline.smsservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.theabconline.smsservice.entity.FormBO;
import org.theabconline.smsservice.mapping.FieldMapping;
import org.theabconline.smsservice.mapping.FormMetadata;
import org.theabconline.smsservice.repository.FormRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ParsingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParsingService.class);
    private final ObjectMapper mapper;
    private final FormRepository formRepository;
    @Value("${jdyun.defaultPath:/data}")
    private String defaultPath;
    @Value("${jdyun.appIdFieldName:appId}")
    private String appIdWidget;
    @Value("${jdyun.entryIdFieldName:entryId}")
    private String entryIdWidget;
    @Value("${jdyun.dataIdFieldName:_id}")
    private String dataIdWidget;

    @Autowired
    public ParsingService(ObjectMapper mapper,
                          FormRepository formRepository) {
        this.mapper = mapper;
        this.formRepository = formRepository;
    }

    String getAppId(String message) throws IOException {
        return getFieldValue(message, appIdWidget);
    }

    String getEntryId(String message) throws IOException {
        return getFieldValue(message, entryIdWidget);
    }

    String getDataId(String message) throws IOException {
        return getFieldValue(message, dataIdWidget);
    }

    FormMetadata getFormMetadata(String message) throws IOException {
        FormBO formBO = getFormBO(message);
        String metadataWidget = formBO.getMetadataWidget();
        String fieldValue = getFieldValue(message, metadataWidget);
        if (Strings.isNullOrEmpty(fieldValue)) {
            return null;
        }
        return mapper.readValue(fieldValue, FormMetadata.class);
    }

    String getPhoneNumbers(String message, String phoneNumbersWidget) throws IOException {
        String result;
        JsonNode phoneNumberField = mapper.readTree(message).at(defaultPath).get(phoneNumbersWidget);

        if (phoneNumberField.isArray()) {
            List phoneNumberList = mapper.convertValue(phoneNumberField, List.class);
            result = Joiner.on(",").join(phoneNumberList);
        } else {
            result = getFieldValue(message, phoneNumbersWidget);
        }

        return result;
    }

    String getPayload(String message, List<FieldMapping> fieldMappings) throws IOException {
        Map<String, String> payloadMap = Maps.newHashMap();
        for (FieldMapping fieldMapping : fieldMappings) {
            String fieldValue = getFieldValue(message, fieldMapping.getWidget());
            payloadMap.put(fieldMapping.getSmsField(), fieldValue);
        }

        return mapper.writeValueAsString(payloadMap);
    }

    String getFieldValue(String message, String fieldName) throws IOException {
        return getFieldValue(message, defaultPath, fieldName);
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

    private FormBO getFormBO(String message) throws IOException {
        String appId = getAppId(message);
        String entryId = getEntryId(message);

        return getFormBO(appId, entryId);
    }

    private FormBO getFormBO(String appId, String entryId) {
        List<FormBO> formBOList = formRepository.findAllByAppIdAndEntryId(appId, entryId);

        if (formBOList.size() != 1 || formBOList.get(0) == null) {
            throw new RuntimeException(String.format("Cannot find form appId: %s, entryId: %s  ", appId, entryId));
        }

        return formBOList.get(0);
    }

}
