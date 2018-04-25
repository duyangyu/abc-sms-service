package org.theabconline.smsservice.config;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Value;

public class Field {

    @Value("${jdy.fieldDefaultPath: /data}")
    private String defaultPath;

    private String path;
    private String fieldName;
    private String templateKey;

    /* Custom code */
    public String getPath() {
        return Strings.isNullOrEmpty(path) ? defaultPath : path;
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
