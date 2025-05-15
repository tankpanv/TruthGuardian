package com.example.truthguardian.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DebunkArticle {
    private int id;
    private String title;
    private String content = ""; // 默认值
    private String summary;
    private String source;
    private List<String> tags;
    private String status;
    private int views = 0; // 默认值
    private int likes = 0; // 默认值
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;
    
    @SerializedName("published_at")
    private String publishedAt;
    
    @SerializedName("author_id")
    private int authorId;
    
    private Author author; // 可能为空
    
    @SerializedName("rumor_reports")
    private List<RumorReport> rumorReports = new ArrayList<>(); // 默认空列表
    
    @SerializedName("clarification_reports")
    private List<ClarificationReport> clarificationReports = new ArrayList<>(); // 默认空列表
    
    private List<Comment> comments = new ArrayList<>(); // 默认空列表
    
    // 作者信息内部类
    public static class Author {
        private int id;
        private String username = "匿名"; // 默认值
        
        @SerializedName("display_name")
        private String displayName = "匿名"; // 默认值

        public int getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 谣言报告内部类
    public static class RumorReport {
        private int id;
        private String title;
        private String content;
        private String status;
        
        @SerializedName("created_at")
        private String createdAt;

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getStatus() {
            return status;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }
    
    // 辟谣素材内部类
    public static class ClarificationReport {
        private int id;
        private String title;
        private String content;
        private String source;
        
        @SerializedName("created_at")
        private String createdAt;

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getSource() {
            return source;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }
    
    // 评论内部类
    public static class Comment {
        private int id;
        private String content;
        private Author user;
        
        @SerializedName("created_at")
        private String createdAt;

        public int getId() {
            return id;
        }

        public String getContent() {
            return content;
        }

        public Author getUser() {
            return user;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getSummary() {
        return summary;
    }

    public String getSource() {
        return source;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getStatus() {
        return status;
    }

    public int getViews() {
        return views;
    }

    public int getLikes() {
        return likes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public String getPublishedAt() {
        return publishedAt;
    }
    
    public int getAuthorId() {
        return authorId;
    }

    public Author getAuthor() {
        // 如果author为null，创建一个默认作者对象
        if (author == null) {
            author = new Author();
        }
        return author;
    }

    public List<RumorReport> getRumorReports() {
        return rumorReports;
    }

    public List<ClarificationReport> getClarificationReports() {
        return clarificationReports;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }
} 