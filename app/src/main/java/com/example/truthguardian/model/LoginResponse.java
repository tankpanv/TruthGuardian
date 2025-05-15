package com.example.truthguardian.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("access_token")
    private String accessToken;
    
    @SerializedName("refresh_token")
    private String refreshToken;
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public boolean isSuccess() {
        return accessToken != null && !accessToken.isEmpty();
    }
    
    public String getToken() {
        return accessToken;
    }
} 