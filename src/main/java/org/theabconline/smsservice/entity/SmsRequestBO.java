package org.theabconline.smsservice.entity;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "SMS_REQUEST")
public class SmsRequestBO {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "biz_id")
    private String bizId;

    @Column(name = "template_code")
    private String templateCode;

    @Column(name = "phone_numbers")
    private String phoneNumbers;

    @Column(name = "payload")
    private String payload;

    @Column(name = "is_sent", columnDefinition = "TINYINT(1)")
    private Boolean isSent;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_on", columnDefinition = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @Column(name = "record_id")
    private Long recordId;

    public SmsRequestBO() {
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

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(String phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Boolean getSent() {
        return isSent;
    }

    public void setSent(Boolean sent) {
        isSent = sent;
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

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }
}
