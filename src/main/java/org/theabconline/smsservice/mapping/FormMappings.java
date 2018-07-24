package org.theabconline.smsservice.mapping;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@PropertySource("classpath:form-mappings.properties")
@ConfigurationProperties
public class FormMappings {

    public static final String ROOT_PATH = "/";
    public static final String DEFAULT_PATH = "/data";

    private List<Form> forms;
    private RegistrationForm registrationForm;

    public List<Form> getForms() {
        return forms;
    }

    public void setForms(List<Form> forms) {
        this.forms = forms;
    }

    public RegistrationForm getRegistrationForm() {
        return registrationForm;
    }

    public void setRegistrationForm(RegistrationForm registrationForm) {
        this.registrationForm = registrationForm;
    }
}
