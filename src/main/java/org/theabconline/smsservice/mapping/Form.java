package org.theabconline.smsservice.mapping;

import org.springframework.stereotype.Component;

@Component
public class Form {

    private String name;
    private String appId;
    private String entryId;

    /* Generated code */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

}
