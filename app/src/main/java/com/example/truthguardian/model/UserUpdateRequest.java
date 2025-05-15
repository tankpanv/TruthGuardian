package com.example.truthguardian.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserUpdateRequest {
    private String name;
    
    @SerializedName("user_name")
    private String userName;
    
    private String phone;
    
    @SerializedName("avatar_url")
    private String avatarUrl;
    
    private String bio;
    
    private List<String> interests;
    
    private List<String> tags;
    
    @SerializedName("old_password")
    private String oldPassword;
    
    @SerializedName("new_password")
    private String newPassword;

    public UserUpdateRequest() {
    }

    // 基本信息更新构造函数
    public UserUpdateRequest(String name, String userName, String phone, String avatarUrl, String bio) {
        this.name = name;
        this.userName = userName;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
} 