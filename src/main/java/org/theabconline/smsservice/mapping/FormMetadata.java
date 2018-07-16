package org.theabconline.smsservice.mapping;

import java.util.List;

public class FormMetadata {
    private List<SmsTemplate> smsTemplates;

    public List<SmsTemplate> getSmsTemplates() {
        return smsTemplates;
    }

    public void setSmsTemplates(List<SmsTemplate> smsTemplates) {
        this.smsTemplates = smsTemplates;
    }
}
