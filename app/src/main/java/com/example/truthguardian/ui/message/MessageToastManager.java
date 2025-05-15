package com.example.truthguardian.ui.message;

import android.content.Context;
import android.view.View;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.example.truthguardian.websocket.WebSocketManager;
import com.google.android.material.snackbar.Snackbar;
import android.app.Activity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import android.widget.TextView;
import com.example.truthguardian.R;

public class MessageToastManager {
    private static final int MAX_CACHED_MESSAGES = 100;
    private Context context;
    private final List<Message> cachedMessages = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MessageToastManager(Context context) {
        this.context = context;
    }

    public void showMessage(@NonNull WebSocketManager.Message wsMessage) {
        Message message = new Message(
            wsMessage.getId(),
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
        
        addMessageToCache(message);
        
        View rootView = ((Activity) context).findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, message.getTitle(), Snackbar.LENGTH_LONG);
        
        // 自定义 Snackbar 的样式
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundResource(R.drawable.snackbar_background);
        
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        if (textView != null) {
            textView.setMaxLines(2);
            textView.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
        
        // 添加动作按钮
        snackbar.setAction("查看", view -> {
            Intent intent = new Intent(context, MessageDetailActivity.class);
            intent.putExtra("message_id", message.getId());
            intent.putExtra("message_title", message.getTitle());
            intent.putExtra("message_content", message.getContent());
            intent.putExtra("message_type", message.getMsgType());
            intent.putExtra("message_timestamp", message.getSendTime());
            context.startActivity(intent);
        });
        
        snackbar.show();
    }

    private void addMessageToCache(Message message) {
        cachedMessages.add(0, message);
        if (cachedMessages.size() > MAX_CACHED_MESSAGES) {
            cachedMessages.remove(cachedMessages.size() - 1);
        }
    }

    public List<Message> getCachedMessages() {
        return new ArrayList<>(cachedMessages);
    }

    public void clearCache() {
        cachedMessages.clear();
    }
} 