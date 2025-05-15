package com.example.truthguardian.model;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {
    private String msg;
    
    @SerializedName("user_id")
    private int userId;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
} 