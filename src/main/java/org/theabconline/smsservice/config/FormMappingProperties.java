package org.theabconline.smsservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;
import java.util.Map;

@Configuration
@PropertySource("classpath:form-mapping.properties")
@ConfigurationProperties
public class FormMappingProperties {

    private List<Form> forms;
    private Map<String, Fields> mappings;

    public List<Form> getForms() {
        return forms;
    }

    public void setForms(List<Form> forms) {
        this.forms = forms;
    }

    public Map<String, Fields> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, Fields> mappings) {
        this.mappings = mappings;
    }

    public static class Form {
        private String appId;
        private String entryId;
        private String name;
        private String templateCode;
        private String phoneNumberPath;
        private String phoneNumberFieldName;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getEntryId() {
            return entryId;
        }

        public void setEntryId(String entryId) {
            this.entryId = entryId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTemplateCode() {
            return templateCode;
        }

        public void setTemplateCode(String templateCode) {
            this.templateCode = templateCode;
        }

        public String getPhoneNumberPath() {
            return phoneNumberPath;
        }

        public void setPhoneNumberPath(String phoneNumberPath) {
            this.phoneNumberPath = phoneNumberPath;
        }

        public String getPhoneNumberFieldName() {
            return phoneNumberFieldName;
        }

        public void setPhoneNumberFieldName(String phoneNumberFieldName) {
            this.phoneNumberFieldName = phoneNumberFieldName;
        }

        public String getFormId() {
            return this.appId + this.entryId;
        }
    }

    public static class Fields {
        private List<Field> fields;

        public List<Field> getFields() {
            return fields;
        }

        public void setFields(List<Field> fields) {
            this.fields = fields;
        }
    }

    public static class Field {
        private String path;
        private String fieldName;
        private String templateKey;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getTemplateKey() {
            return templateKey;
        }

        public void setTemplateKey(String templateKey) {
            this.templateKey = templateKey;
        }
    }
}
