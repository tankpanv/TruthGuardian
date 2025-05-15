package com.example.truthguardian.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DebunkArticleRequest {
    private String title;
    private String content;
    private String summary;
    private String source;
    private List<String> tags;
    
    @SerializedName("rumor_reports")
    private List<Integer> rumorReports;
    
    @SerializedName("clarification_reports")
    private List<Integer> clarificationReports;

    public DebunkArticleRequest(String title, String content, String summary, String source, 
                               List<String> tags, List<Integer> rumorReports, 
                               List<Integer> clarificationReports) {
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.source = source;
        this.tags = tags;
        this.rumorReports = rumorReports;
        this.clarificationReports = clarificationReports;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<Integer> getRumorReports() {
        return rumorReports;
    }

    public void setRumorReports(List<Integer> rumorReports) {
        this.rumorReports = rumorReports;
    }

    public List<Integer> getClarificationReports() {
        return clarificationReports;
    }

    public void setClarificationReports(List<Integer> clarificationReports) {
        this.clarificationReports = clarificationReports;
    }
} 