package com.example.truthguardian.model;

public class DebunkArticleStatusRequest {
    private String status;

    public DebunkArticleStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 