package com.example.truthguardian.websocket;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.net.URISyntaxException;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    private static final String SOCKET_URL = "ws://47.106.74.86:5005"; // 替换为实际的WebSocket URL
    private Socket socket;
    private String token;
    private WebSocketListener listener;
    private Gson gson;
    private static WebSocketManager instance;
    private boolean isConnecting = false;

    // 消息数据结构
    public static class Message {
        @SerializedName("id")
        private int id;
        
        @SerializedName("sender_id")
        private int senderId;
        
        @SerializedName("receiver_id")
        private int receiverId;
        
        @SerializedName("title")
        private String title;
        
        @SerializedName("msg_type")
        private String msgType;
        
        @SerializedName("content")
        private String content;
        
        @SerializedName("priority")
        private int priority;
        
        @SerializedName("is_read")
        private boolean isRead;
        
        @SerializedName("send_time")
        private String sendTime;
        
        @SerializedName("read_time")
        private String readTime;
        
        @SerializedName("expire_time")
        private String expireTime;

        // Getters
        public int getId() { return id; }
        public int getSenderId() { return senderId; }
        public int getReceiverId() { return receiverId; }
        public String getTitle() { return title; }
        public String getMsgType() { return msgType; }
        public String getContent() { return content; }
        public int getPriority() { return priority; }
        public boolean isRead() { return isRead; }
        public String getSendTime() { return sendTime; }
        public String getReadTime() { return readTime; }
        public String getExpireTime() { return expireTime; }
        public void setRead(boolean read) { isRead = read; }
    }

    // 连接响应数据结构
    public static class ConnectResponse {
        @SerializedName("success")
        private boolean success;
        
        @SerializedName("message")
        private String message;
        
        @SerializedName("user_id")
        private int userId;

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getUserId() { return userId; }
    }

    // 监听器接口
    public interface WebSocketListener {
        void onConnected(int userId);
        void onDisconnected();
        void onNewMessage(Message message);
        void onError(String error);
    }

    private WebSocketManager(String token, WebSocketListener listener) {
        this.token = token;
        this.listener = listener;
        this.gson = new Gson();
        Log.d(TAG, "WebSocketManager initialized with token");
    }

    public static synchronized WebSocketManager getInstance(String token, WebSocketListener listener) {
        if (instance == null) {
            Log.d(TAG, "Creating new WebSocketManager instance");
            instance = new WebSocketManager(token, listener);
        } else if (token != null && listener != null) {
            Log.d(TAG, "Updating existing WebSocketManager instance");
            instance.token = token;
            instance.listener = listener;
        }
        return instance;
    }

    public void connect() {
        if (isConnecting) {
            Log.d(TAG, "Already attempting to connect, ignoring duplicate request");
            return;
        }

        if (isConnected()) {
            Log.d(TAG, "Already connected, ignoring connect request");
            return;
        }

        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Cannot connect: token is null or empty");
            if (listener != null) {
                listener.onError("无效的认证令牌");
            }
            return;
        }

        try {
            Log.d(TAG, "Initializing Socket.IO connection to " + SOCKET_URL);
            isConnecting = true;

            // 配置Socket.IO
            IO.Options options = new IO.Options();
            options.forceNew = true;
            options.reconnection = true;
            options.reconnectionAttempts = 3;
            options.reconnectionDelay = 1000;
            options.timeout = 20000;
            options.transports = new String[]{"websocket"};
            
            // 设置认证头 - 确保token格式正确
            String formattedToken = token;
            if (!formattedToken.startsWith("Bearer ")) {
                formattedToken = "Bearer " + formattedToken;
            }
            Log.d(TAG, "Using token format: " + (formattedToken.startsWith("Bearer ") ? "Bearer + token" : "token without Bearer"));
            
            options.extraHeaders = new HashMap<>();
            options.extraHeaders.put("Authorization", Arrays.asList(formattedToken));

            // 创建Socket实例
            socket = IO.socket(SOCKET_URL, options);
            
            // 设置事件监听器
            setupEventListeners();

            // 连接到服务器
            Log.d(TAG, "Attempting to connect to WebSocket server...");
            socket.connect();

        } catch (URISyntaxException e) {
            Log.e(TAG, "Invalid Socket.IO URI", e);
            isConnecting = false;
            if (listener != null) {
                listener.onError("无效的服务器地址: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Socket.IO", e);
            isConnecting = false;
            if (listener != null) {
                listener.onError("连接初始化失败: " + e.getMessage());
            }
        }
    }

    private void setupEventListeners() {
        if (socket == null) {
            Log.e(TAG, "Cannot setup listeners: socket is null");
            return;
        }

        socket.on(Socket.EVENT_CONNECT, args -> {
            Log.d(TAG, "Socket.IO connected");
            isConnecting = false;
            // 连接成功后直接触发onConnected
            if (listener != null) {
                // 临时使用1作为用户ID，实际应该从服务器获取
                listener.onConnected(1);
            }
        });

        socket.on(Socket.EVENT_DISCONNECT, args -> {
            Log.d(TAG, "Socket.IO disconnected");
            isConnecting = false;
            if (listener != null) {
                listener.onDisconnected();
            }
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            String errorMsg = args.length > 0 ? args[0].toString() : "Unknown error";
            Log.e(TAG, "Socket.IO connection error: " + errorMsg);
            isConnecting = false;
            if (listener != null) {
                listener.onError("连接错误: " + errorMsg);
            }
        });

        // 新消息事件
        socket.on("new_message", args -> {
            try {
                Log.d(TAG, "Received new_message event, args length: " + args.length);
                if (args.length > 0) {
                    Log.d(TAG, "new_message rawData: " + args[0]);
                }
                Object rawData = args[0];
                Message message;
                
                if (rawData instanceof String) {
                    // 如果是字符串，直接解析
                    message = gson.fromJson((String) rawData, Message.class);
                } else if (rawData instanceof JSONObject) {
                    // 如果是JSONObject，转换为字符串后解析
                    message = gson.fromJson(((JSONObject) rawData).toString(), Message.class);
                } else {
                    throw new IllegalArgumentException("Unexpected data type: " + rawData.getClass().getName());
                }
                
                Log.d(TAG, "Parsed message: " + message.getTitle() + ", id: " + message.getId());
                if (listener != null) {
                    Log.d(TAG, "Dispatching message to listener.onNewMessage");
                    listener.onNewMessage(message);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing new_message", e);
                if (listener != null) {
                    listener.onError("解析消息失败: " + e.getMessage());
                }
            }
        });
    }

    public void disconnect() {
        if (socket != null) {
            Log.d(TAG, "Disconnecting Socket.IO");
            socket.disconnect();
            socket = null;
        }
        isConnecting = false;
    }

    public boolean isConnected() {
        return socket != null && socket.connected();
    }

    public void sendMessage(String event, JSONObject data) {
        if (isConnected()) {
            Log.d(TAG, "Sending message: " + event);
            socket.emit(event, data);
        } else {
            Log.e(TAG, "Cannot send message: not connected");
            if (listener != null) {
                listener.onError("无法发送消息：WebSocket未连接");
            }
        }
    }
} 