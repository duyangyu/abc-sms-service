package org.theabconline.smsservice.entity;


import javax.persistence.*;
import java.util.Date;

@Entity(name = "raw_message")
public class RawMessageBO {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Lob
    @Column(name = "message")
    private String message;

    @Column(name = "is_processed", columnDefinition = "TINYINT(1)")
    private Boolean isProcessed;

    @Column(name = "created_on", columnDefinition = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @Column(name = "processed_on", columnDefinition = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date processedOn;

    public RawMessageBO() {
    }

    @PrePersist
    public void onPrePersist() {
        createdOn = new Date();
    }

    @PreUpdate
    public void onPreUpdate() {
        processedOn = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getProcessed() {
        return isProcessed;
    }

    public void setProcessed(Boolean processed) {
        isProcessed = processed;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getProcessedOn() {
        return processedOn;
    }

    public void setProcessedOn(Date processedOn) {
        this.processedOn = processedOn;
    }
}
