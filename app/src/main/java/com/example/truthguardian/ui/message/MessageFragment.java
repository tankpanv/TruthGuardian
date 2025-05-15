package com.example.truthguardian.ui.message;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.truthguardian.MainActivity;
import com.example.truthguardian.R;
import com.example.truthguardian.api.ApiClient;
import com.example.truthguardian.api.MessageService;
import com.example.truthguardian.websocket.WebSocketManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageFragment extends Fragment implements MessageAdapter.OnMessageClickListener {
    private static final String TAG = "MessageFragment";
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_TOKEN = "access_token";
    
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private View emptyView;
    private WebSocketManager webSocketManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        initializeWebSocket(context);
    }

    private void initializeWebSocket(Context context) {
        // 从SharedPreferences获取token
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String token = sharedPreferences.getString(KEY_TOKEN, null);

        if (token == null) {
            Log.e(TAG, "Token not found in SharedPreferences");
            return;
        }

        // 获取WebSocketManager实例
        webSocketManager = WebSocketManager.getInstance(token, new WebSocketManager.WebSocketListener() {
            @Override
            public void onConnected(int userId) {
                Log.d(TAG, "WebSocket connected, userId: " + userId);
                // 连接成功，加载历史消息
                loadHistoryMessages();
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "WebSocket disconnected");
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(context, "WebSocket连接已断开", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onNewMessage(WebSocketManager.Message wsMessage) {
                Log.d(TAG, "New message received: " + wsMessage.getTitle());
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Message uiMessage = new Message(
                            String.valueOf(wsMessage.getId()),
                            wsMessage.getSenderId(),
                            wsMessage.getReceiverId(),
                            wsMessage.getTitle(),
                            wsMessage.getContent(),
                            wsMessage.getMsgType(),
                            wsMessage.getPriority(),
                            wsMessage.getSendTime(),
                            wsMessage.getExpireTime(),
                            wsMessage.isRead()
                        );
                        adapter.addMessage(uiMessage);
                        updateEmptyView();
                        // 恢复推送弹窗
                        if (getActivity() instanceof com.example.truthguardian.MainActivity) {
                            ((com.example.truthguardian.MainActivity) getActivity()).getMessageToastManager().showMessage(wsMessage);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "WebSocket error: " + error);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(context, "消息接收错误: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });

        // 确保WebSocket连接
        if (webSocketManager != null && !webSocketManager.isConnected()) {
            Log.d(TAG, "Connecting to WebSocket...");
            webSocketManager.connect();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_message, container, false);
        
        recyclerView = root.findViewById(R.id.recycler_view_messages);
        emptyView = root.findViewById(R.id.empty_view);
        
        adapter = new MessageAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        // 进入页面直接加载历史消息
        adapter.setMessages(null);
        updateEmptyView();
        loadHistoryMessages();
        
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 不再直接操作messages，全部通过adapter.setMessages刷新
        // 保持与历史消息刷新一致
    }

    private void loadHistoryMessages() {
        Context context = getContext();
        if (context == null) return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String token = sharedPreferences.getString(KEY_TOKEN, null);
        if (token == null) return;
        if (!token.startsWith("Bearer ")) {
            token = "Bearer " + token;
        }
        MessageService messageService = ApiClient.getClient().create(MessageService.class);
        messageService.getHistoryMessages(token, "all", 1, 20)
            .enqueue(new Callback<MessageService.HistoryResponse>() {
                @Override
                public void onResponse(Call<MessageService.HistoryResponse> call, Response<MessageService.HistoryResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                        List<Message> history = null;
                        if (response.body().data != null) {
                            if (response.body().data.messages != null && !response.body().data.messages.isEmpty()) {
                                history = response.body().data.messages;
                            } else if (response.body().data.message != null) {
                                history = new ArrayList<>();
                                history.add(response.body().data.message);
                            }
                        }
                        if (history != null && !history.isEmpty()) {
                            final List<Message> finalHistory = history;
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    adapter.setMessages(finalHistory);
                                    updateEmptyView();
                                });
                            }
                        }
                    } else {
                        Log.e(TAG, "历史消息获取失败: " + (response.body() != null ? response.body().message : "null"));
                    }
                }
                @Override
                public void onFailure(Call<MessageService.HistoryResponse> call, Throwable t) {
                    Log.e(TAG, "历史消息请求失败", t);
                }
            });
    }

    @Override
    public void onMessageClick(Message message) {
        try {
            message.setUnread(false);
            adapter.markMessageAsRead(message.getId());
            
            // 跳转到详情页
            android.content.Intent intent = new android.content.Intent(requireContext(), MessageDetailActivity.class);
            intent.putExtra(MessageDetailActivity.EXTRA_TITLE, message.getTitle());
            intent.putExtra(MessageDetailActivity.EXTRA_CONTENT, message.getContent());
            intent.putExtra(MessageDetailActivity.EXTRA_TIME, message.getDisplayTime());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening message detail", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "无法打开消息详情", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateEmptyView() {
        if (adapter.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 不要在这里断开WebSocket连接，因为它是全局单例
        // WebSocket的连接和断开应该由MainActivity管理
    }
} 