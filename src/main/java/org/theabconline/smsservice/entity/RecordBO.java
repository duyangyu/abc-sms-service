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

    @Column(name = "update_count")
    private Integer updateCount;

    @Column(name = "updated_on", columnDefinition = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn;

    @Column(name = "raw_message_id")
    private Long rawMessageId;

    public RecordBO() {
    }

    @PrePersist
    public void onPrePersist() {
        Date date = new Date();
        createdOn = date;
        updatedOn = date;
        updateCount = 0;
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedOn = new Date();
        updateCount += 1;
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

    public Integer getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(Integer updateCount) {
        this.updateCount = updateCount;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Long getRawMessageId() {
        return rawMessageId;
    }

    public void setRawMessageId(Long rawMessageId) {
        this.rawMessageId = rawMessageId;
    }
}
