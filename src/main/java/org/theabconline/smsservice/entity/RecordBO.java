package org.theabconline.smsservice.entity;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "RECORD")
public class RecordBO {

    @Id
    @GeneratedValue
    @Column(name = "id")
    Long id;

    @Column(name = "app_id")
    private String appId;

    @Column(name = "entry_id")
    private String entryId;

    @Column(name = "data_id")
    private String dataId;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_on", columnDefinition = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @Column(name = "updated_on_1", columnDefinition = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn1;

    @Column(name = "updated_on_2", columnDefinition = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn2;

    @Column(name = "raw_message_id")
    private Long rawMessageId;

    public RecordBO() {
    }

    @PrePersist
    public void onPrePersist() {
        createdOn = new Date();
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

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getUpdatedOn1() {
        return updatedOn1;
    }

    public void setUpdatedOn1(Date updatedOn1) {
        this.updatedOn1 = updatedOn1;
    }

    public Date getUpdatedOn2() {
        return updatedOn2;
    }

    public void setUpdatedOn2(Date updatedOn2) {
        this.updatedOn2 = updatedOn2;
    }

    public Long getRawMessageId() {
        return rawMessageId;
    }

    public void setRawMessageId(Long rawMessageId) {
        this.rawMessageId = rawMessageId;
    }
}
