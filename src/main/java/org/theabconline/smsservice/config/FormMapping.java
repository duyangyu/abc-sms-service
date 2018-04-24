package org.theabconline.smsservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;
import java.util.Map;

@Configuration
@PropertySource("classpath:form-mapping.properties")
@ConfigurationProperties
public class FormMapping {

    private List<Form> forms;
    private Map<String, Recipients> mappings;

    public List<Form> getForms() {
        return forms;
    }

    public void setForms(List<Form> forms) {
        this.forms = forms;
    }

    public Map<String, Recipients> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, Recipients> mappings) {
        this.mappings = mappings;
    }
}
