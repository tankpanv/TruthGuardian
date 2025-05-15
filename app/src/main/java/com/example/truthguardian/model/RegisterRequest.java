package com.example.truthguardian.model;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("user_name")
    private String userName;
    
    private String password;
    
    private String name;
    
    @SerializedName("phone")
    private String phone;

    public RegisterRequest(String userName, String password, String name, String phone) {
        this.userName = userName;
        this.password = password;
        this.name = name;
        this.phone = phone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
} 