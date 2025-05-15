package com.example.truthguardian.model;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private User data;

    public boolean isSuccess() {
        return success || (getId() != null);
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getData() {
        return data;
    }

    public void setData(User data) {
        this.data = data;
    }

    public Integer getId() {
        return data != null ? data.getId() : null;
    }

    public String getUserName() {
        return data != null ? data.getUserName() : null;
    }

    public String getName() {
        return data != null ? data.getName() : null;
    }

    public String getAvatarUrl() {
        return data != null ? data.getAvatarUrl() : null;
    }
} 