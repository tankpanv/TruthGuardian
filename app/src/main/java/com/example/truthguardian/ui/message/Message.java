package com.example.truthguardian.ui.message;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Message {
    @SerializedName("id")
    private Object id;
    @SerializedName("sender_id")
    private int senderId;
    @SerializedName("receiver_id")
    private int receiverId;
    @SerializedName("title")
    private String title;
    @SerializedName("content")
    private String content;
    @SerializedName("msg_type")
    private String msgType;
    @SerializedName("priority")
    private int priority;
    @SerializedName("send_time")
    private String sendTime;
    @SerializedName("read_time")
    private String readTime;
    @SerializedName("expire_time")
    private String expireTime;
    @SerializedName("is_read")
    private boolean isRead;

    // 兼容旧逻辑
    private long timestamp;
    private boolean unread;

    public Message(Object id, String title, String content, long timestamp) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.unread = true;
    }

    public Message(Object id, String title, String content, Date time, boolean unread) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = time.getTime();
        this.unread = unread;
    }

    // 新增：API用构造
    public Message(Object id, int senderId, int receiverId, String title, String content, String msgType, int priority, String sendTime, String expireTime, boolean isRead) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.title = title;
        this.content = content;
        this.msgType = msgType;
        this.priority = priority;
        this.sendTime = sendTime;
        this.expireTime = expireTime;
        this.isRead = isRead;
        this.unread = !isRead;
        // 设置 timestamp 为 sendTime 的时间戳，用于兼容旧逻辑
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            Date date = sdf.parse(sendTime);
            if (date != null) {
                this.timestamp = date.getTime();
            }
        } catch (Exception e) {
            this.timestamp = System.currentTimeMillis();
        }
    }

    public String getId() { return id == null ? null : String.valueOf(id); }
    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getMsgType() { return msgType; }
    public int getPriority() { return priority; }
    public String getSendTime() { return sendTime; }
    public String getReadTime() { return readTime; }
    public String getExpireTime() { return expireTime; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { 
        isRead = read; 
        this.unread = !read;
        if (read && readTime == null) {
            readTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new Date());
        }
    }

    // 兼容旧逻辑
    public long getTimestamp() { return timestamp; }
    public Date getDate() { return new Date(timestamp); }
    public Date getTime() { return getDate(); }
    public boolean isUnread() { return unread; }
    public void setUnread(boolean unread) { 
        this.unread = unread; 
        this.isRead = !unread;
        if (!unread && readTime == null) {
            readTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new Date());
        }
    }

    public String getDisplayTime() {
        if (sendTime != null && !sendTime.isEmpty()) {
            // 只显示到分钟
            return sendTime.length() > 16 ? sendTime.substring(0, 16) : sendTime;
        }
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(getDate());
    }
} 