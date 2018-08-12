package org.theabconline.smsservice.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "form")
public class FormBO {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "app_id")
    private String appId;

    @Column(name = "entry_id")
    private String entryId;

    @Column(name = "metadata_widget")
    private String metadataWidget;

    public FormBO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getMetadataWidget() {
        return metadataWidget;
    }

    public void setMetadataWidget(String metadataWidget) {
        this.metadataWidget = metadataWidget;
    }

}
