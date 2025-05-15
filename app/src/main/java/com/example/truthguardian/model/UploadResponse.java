package com.example.truthguardian.model;

public class UploadResponse {
    private boolean success;
    private String file_path;
    private String file_name;
    private long file_size;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getFilePath() {
        return file_path;
    }

    public void setFilePath(String file_path) {
        this.file_path = file_path;
    }

    public String getFileName() {
        return file_name;
    }

    public void setFileName(String file_name) {
        this.file_name = file_name;
    }

    public long getFileSize() {
        return file_size;
    }

    public void setFileSize(long file_size) {
        this.file_size = file_size;
    }
} 