package org.theabconline.smsservice.entity;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "sms_message")
public class SmsMessageBO {

    @Id
    @GeneratedValue
    @Column(name = "id")
    Long id;

    @Column(name = "phone_number")
    String phoneNumber;

    @Column(name = "content")
    String content;

    @Column(name = "biz_id")
    private String bizId;

    @Column(name = "is_sent", columnDefinition = "TINYINT(1)")
    private Boolean isSent;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "sent_on", columnDefinition = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sentOn;

    @Column(name = "updated_on", columnDefinition = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn;

    @Column(name = "sms_request_id")
    private Long smsRequestId;

    @Column(name = "record_id")
    private Long recordId;

    public SmsMessageBO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
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

    public Date getSentOn() {
        return sentOn;
    }

    public void setSentOn(Date sentOn) {
        this.sentOn = sentOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Long getSmsRequestId() {
        return smsRequestId;
    }

    public void setSmsRequestId(Long smsRequestId) {
        this.smsRequestId = smsRequestId;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }
}
