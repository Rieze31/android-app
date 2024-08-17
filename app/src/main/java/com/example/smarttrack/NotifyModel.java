package com.example.smarttrack;

public class NotifyModel {
    private String senderEmail;
    private String senderName;
    private String recipientEmail;
    private String message;

    // Default constructor (required by Firestore)
    public NotifyModel() {
    }

    // Constructor with parameters
    public NotifyModel(String senderName,String senderEmail,String recipientEmail, String message) {
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.recipientEmail = recipientEmail;
        this.message = message;
    }

    // Getter and setter methods for all fields
    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
