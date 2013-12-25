package com.smartengine.ohnomessaging.model;

public class TextMessage {
    
    private String phoneNumber;
    private String contactName;
    private int contactId;
    private int id;
    private int threadId;
    private int messaageType;
    private String messageBody;
    private String timeOfMessage;
    private int messageCount;
    
    public TextMessage() {
        // TODO Auto-generated constructor stub
    }

    public TextMessage(String phoneNumber, String contactName, int contactId, int id, int threadId, int messaageType,
            String messageBody, String timeOfMessage) {
        this.phoneNumber = phoneNumber;
        this.contactName = contactName;
        this.contactId = contactId;
        this.id = id;
        this.threadId = threadId;
        this.messaageType = messaageType;
        this.messageBody = messageBody;
        this.timeOfMessage = timeOfMessage;
        this.messageCount = 0;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public int getMessaageType() {
        return messaageType;
    }

    public void setMessaageType(int messaageType) {
        this.messaageType = messaageType;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getTimeOfMessage() {
        return timeOfMessage;
    }

    public void setTimeOfMessage(String timeOfMessage) {
        this.timeOfMessage = timeOfMessage;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    } 
    
}
