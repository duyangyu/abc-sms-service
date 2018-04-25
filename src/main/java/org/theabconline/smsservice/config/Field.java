package org.theabconline.smsservice.config;

import com.google.common.base.Strings;

public class Field {

    private String path;
    private String fieldName;
    private String templateKey;

    /* Custom code */
    public String getPath() {
        return Strings.isNullOrEmpty(path) ? FormMappings.DEFAULT_PATH : path;
    }

    /* Generated code */
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
