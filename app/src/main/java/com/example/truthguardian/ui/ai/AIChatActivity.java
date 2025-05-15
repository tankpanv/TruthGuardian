package com.example.truthguardian.ui.ai;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truthguardian.R;
import com.example.truthguardian.api.AIService;
import com.example.truthguardian.api.ApiClient;
import com.example.truthguardian.model.AIChatMessage;
import com.example.truthguardian.model.AIChatRequest;
import com.example.truthguardian.model.AIChatResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AIChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ProgressBar loadingProgressBar;
    private AIChatAdapter chatAdapter;
    private List<AIChatMessage> chatMessages;
    private AIService aiService;
    
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_TOKEN = "access_token";
    private static final String TAG = "AIChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);
        
        // 初始化视图
        recyclerView = findViewById(R.id.recyclerViewChat);
        messageEditText = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.buttonSend);
        loadingProgressBar = findViewById(R.id.progressBarLoading);
        
        // 设置返回按钮
        findViewById(R.id.buttonBack).setOnClickListener(v -> onBackPressed());
        
        // 初始化聊天消息列表
        chatMessages = new ArrayList<>();
        
        // 添加欢迎消息
        AIChatMessage welcomeMessage = new AIChatMessage(
                "你好！我是一位专业的辟谣专家，有什么我能帮你的吗？", 
                AIChatMessage.TYPE_RECEIVED);
        chatMessages.add(welcomeMessage);
        
        // 设置RecyclerView
        chatAdapter = new AIChatAdapter(chatMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
        
        // 初始化API服务
        aiService = ApiClient.getClient().create(AIService.class);
        
        // 设置发送按钮点击事件
        sendButton.setOnClickListener(v -> sendMessage());
    }
    
    private void sendMessage() {
        String message = messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }
        
        // 添加用户消息到列表
        AIChatMessage userMessage = new AIChatMessage(message, AIChatMessage.TYPE_SENT);
        chatMessages.add(userMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.scrollToPosition(chatMessages.size() - 1);
        
        // 清空输入框
        messageEditText.setText("");
        
        // 显示加载动画
        loadingProgressBar.setVisibility(View.VISIBLE);
        sendButton.setEnabled(false);
        
        // 添加一条"正在思考"的临时消息
        AIChatMessage thinkingMessage = new AIChatMessage("正在思考中...", AIChatMessage.TYPE_RECEIVED);
        chatMessages.add(thinkingMessage);
        int thinkingMessagePosition = chatMessages.size() - 1;
        chatAdapter.notifyItemInserted(thinkingMessagePosition);
        recyclerView.scrollToPosition(thinkingMessagePosition);
        
        // 准备请求数据
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String token = sharedPreferences.getString(KEY_TOKEN, "");
        
        // 检查token是否为空
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Authorization token is null or empty");
            loadingProgressBar.setVisibility(View.GONE);
            sendButton.setEnabled(true);
            
            // 移除"正在思考"的临时消息
            chatMessages.remove(thinkingMessagePosition);
            chatAdapter.notifyItemRemoved(thinkingMessagePosition);
            
            // 添加错误提示消息
            AIChatMessage errorMessage = new AIChatMessage("您尚未登录或登录已过期，请先登录。", AIChatMessage.TYPE_RECEIVED);
            chatMessages.add(errorMessage);
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            recyclerView.scrollToPosition(chatMessages.size() - 1);
            
            Toast.makeText(this, "请先登录再使用AI聊天功能", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 确保token格式正确，避免重复添加Bearer前缀
        String authHeader;
        if (token.startsWith("Bearer ")) {
            authHeader = token;
        } else {
            authHeader = "Bearer " + token;
        }
        Log.d(TAG, "Using authorization header: " + authHeader);
        
        AIChatRequest chatRequest = new AIChatRequest(message);
        
        // 发送请求
        aiService.getChatResponse(authHeader, chatRequest).enqueue(new Callback<AIChatResponse>() {
            @Override
            public void onResponse(Call<AIChatResponse> call, Response<AIChatResponse> response) {
                loadingProgressBar.setVisibility(View.GONE);
                sendButton.setEnabled(true);
                
                
                // 移除"正在思考"的临时消息
                chatMessages.remove(thinkingMessagePosition);
                chatAdapter.notifyItemRemoved(thinkingMessagePosition);
                
                if (response.isSuccessful() && response.body() != null) {
                    AIChatResponse chatResponse = response.body();
                    String responseText = chatResponse.getContent();
                    
                    if (!TextUtils.isEmpty(responseText)) {
                        // 添加AI回复到列表
                        AIChatMessage aiMessage = new AIChatMessage(responseText, AIChatMessage.TYPE_RECEIVED);
                        chatMessages.add(aiMessage);
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        recyclerView.scrollToPosition(chatMessages.size() - 1);
                    } else {
                        Toast.makeText(AIChatActivity.this, "AI助手没有回复", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    int errorCode = response.code();
                    String errorMessage = "获取回复失败: " + errorCode;
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(AIChatActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    
                    // 添加错误提示消息
                    AIChatMessage errorMessage2 = new AIChatMessage("抱歉，我遇到了一些问题，请稍后再试。", AIChatMessage.TYPE_RECEIVED);
                    chatMessages.add(errorMessage2);
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    recyclerView.scrollToPosition(chatMessages.size() - 1);
                }
            }
            
            @Override
            public void onFailure(Call<AIChatResponse> call, Throwable t) {
                loadingProgressBar.setVisibility(View.GONE);
                sendButton.setEnabled(true);
                
                // 移除"正在思考"的临时消息
                chatMessages.remove(thinkingMessagePosition);
                chatAdapter.notifyItemRemoved(thinkingMessagePosition);
                
                Toast.makeText(AIChatActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                
                // 添加错误提示消息
                AIChatMessage errorMessage = new AIChatMessage("抱歉，网络连接出现问题，请检查您的网络连接后再试。", AIChatMessage.TYPE_RECEIVED);
                chatMessages.add(errorMessage);
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                recyclerView.scrollToPosition(chatMessages.size() - 1);
            }
        });
    }
} 