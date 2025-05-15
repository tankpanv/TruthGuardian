package com.example.truthguardian.model;

public class AIChatMessage {
    public static final int TYPE_SENT = 1;
    public static final int TYPE_RECEIVED = 2;
    
    private String content;
    private int type;
    private long timestamp;
    
    public AIChatMessage(String content, int type) {
        this.content = content;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 