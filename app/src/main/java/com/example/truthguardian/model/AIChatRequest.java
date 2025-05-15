package com.example.truthguardian.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AIChatRequest {
    
    @SerializedName("provider")
    private final String provider = "dify";
    
    @SerializedName("messages")
    private final List<ChatMessage> messages;
    
    @SerializedName("stream")
    private final boolean stream = false;
    
    @SerializedName("user")
    private final String user = "user123";
    
    @SerializedName("inputs")
    private final Map<String, String> inputs;
    
    public static class ChatMessage {
        @SerializedName("role")
        private String role;
        
        @SerializedName("content")
        private String content;
        
        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
    
    public AIChatRequest(String userMessage) {
        this.messages = new ArrayList<>();
        this.messages.add(new ChatMessage("user", userMessage));
        
        this.inputs = new HashMap<>();
        this.inputs.put("source_url", "http://example.com/news/123");
        this.inputs.put("additional_context", "这是一条关于AI的新闻");
    }
    
    public String getProvider() {
        return provider;
    }
    
    public List<ChatMessage> getMessages() {
        return messages;
    }
    
    public boolean isStream() {
        return stream;
    }
    
    public String getUser() {
        return user;
    }
    
    public Map<String, String> getInputs() {
        return inputs;
    }
} 