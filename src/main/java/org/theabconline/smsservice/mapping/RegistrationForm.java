package org.theabconline.smsservice.mapping;

import com.google.common.base.Strings;

public class RegistrationForm {

    private String fieldsPath;
    private String nameFieldName;
    private String emailFieldName;
    private String mobileFieldName;

    /* Custom code */
    public String getFieldsPath() {
        String actualPath = Strings.isNullOrEmpty(fieldsPath) ? FormMappings.DEFAULT_PATH : fieldsPath;
        return FormMappings.ROOT_PATH.equals(actualPath) ? "" : actualPath;
    }

    /* Generated code */
    public void setFieldsPath(String fieldsPath) {
        this.fieldsPath = fieldsPath;
    }

    public String getNameFieldName() {
        return nameFieldName;
    }

    public void setNameFieldName(String nameFieldName) {
        this.nameFieldName = nameFieldName;
    }

    public String getEmailFieldName() {
        return emailFieldName;
    }

    public void setEmailFieldName(String emailFieldName) {
        this.emailFieldName = emailFieldName;
    }

    public String getMobileFieldName() {
        return mobileFieldName;
    }

    public void setMobileFieldName(String mobileFieldName) {
        this.mobileFieldName = mobileFieldName;
    }
}
