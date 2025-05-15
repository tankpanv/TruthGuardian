package com.example.truthguardian.model;

import com.google.gson.annotations.SerializedName;

public class DebunkArticleResponse {
    private String status;
    
    @SerializedName("article")
    private DebunkArticle article;
    
    private String message;
    
    @SerializedName("error_code")
    private String errorCode;

    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    public DebunkArticle getData() {
        // 注意: API返回的JSON使用"article"字段, 而不是"data"字段
        // 这个方法是为了兼容原有代码，保持向后兼容性
        return getArticle();
    }
    
    public DebunkArticle getArticle() {
        return article;
    }
    
    public void setArticle(DebunkArticle article) {
        this.article = article;
    }

    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    @Override
    public String toString() {
        return "DebunkArticleResponse{" +
                "status='" + status + '\'' +
                ", article=" + (article != null ? "非空" : "null") +
                ", message='" + message + '\'' +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
} 