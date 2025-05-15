package com.example.truthguardian.model;

import com.google.gson.annotations.SerializedName;

public class AIChatResponse {
    
    private String id;
    private String model;
    private String object;
    private long created;
    private String provider;
    
    @SerializedName("server_latency_ms")
    private int serverLatencyMs;
    
    private Choice[] choices;
    
    public static class Choice {
        @SerializedName("finish_reason")
        private String finishReason;
        
        private int index;
        private Message message;
        
        public String getFinishReason() {
            return finishReason;
        }
        
        public int getIndex() {
            return index;
        }
        
        public Message getMessage() {
            return message;
        }
    }
    
    public static class Message {
        private String content;
        private String role;
        
        public String getContent() {
            return content;
        }
        
        public String getRole() {
            return role;
        }
    }
    
    public String getId() {
        return id;
    }
    
    public String getModel() {
        return model;
    }
    
    public String getObject() {
        return object;
    }
    
    public long getCreated() {
        return created;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public int getServerLatencyMs() {
        return serverLatencyMs;
    }
    
    public Choice[] getChoices() {
        return choices;
    }
    
    // 获取实际内容
    public String getContent() {
        if (choices != null && choices.length > 0 && choices[0].message != null) {
            return choices[0].message.content;
        }
        return "无回复内容";
    }
} 