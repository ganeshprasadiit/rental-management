package com.rental.model;

import jakarta.persistence.*;

@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long propertyId;

    // e.g. "EPC", "Gas Safety", "EICR", "Legionella Risk Assessment", "PAT Testing"
    @Column(nullable = false)
    private String certificateType;

    @Column(nullable = false)
    private String fileName;

    // Object key in the R2 bucket - not shown to the user, used to fetch/delete the file
    @Column(nullable = false)
    private String fileKey;

    private Long fileSize;

    private String issueDate;
    private String expiryDate;
    private String uploadedDate;

    @Column(length = 500)
    private String notes;

    // Tracks the last expiry milestone (e.g. "30", "14", "7", "1", "EXPIRED") a reminder
    // was sent for, so the daily scheduler doesn't send duplicate emails for the same milestone
    private String lastReminderMilestone;

    public Certificate() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(String uploadedDate) {
        this.uploadedDate = uploadedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getLastReminderMilestone() {
        return lastReminderMilestone;
    }

    public void setLastReminderMilestone(String lastReminderMilestone) {
        this.lastReminderMilestone = lastReminderMilestone;
    }
}
