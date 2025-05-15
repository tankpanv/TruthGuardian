package com.example.truthguardian.model;

import java.util.Date;

public class Message {
    private String type;
    private String content;
    private Date timestamp;

    public Message(String type, String content, Date timestamp) {
        this.type = type;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
} 