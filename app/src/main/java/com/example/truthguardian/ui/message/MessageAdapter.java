package com.example.truthguardian.ui.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.truthguardian.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages;
    private OnMessageClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnMessageClickListener {
        void onMessageClick(Message message);
    }

    public MessageAdapter(OnMessageClickListener listener) {
        this.messages = new ArrayList<>();
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.titleView.setText(message.getTitle());
        holder.contentView.setText(message.getContent());
        holder.timeView.setText(message.getDisplayTime());
        
        // 设置未读指示器的可见性
        holder.unreadIndicator.setVisibility(message.isUnread() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMessageClick(message);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<Message> messages) {
        if (messages == null) {
            this.messages = new ArrayList<>();
        } else {
            this.messages = new ArrayList<>(messages); // 拷贝一份，防止外部clear影响
        }
        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        List<Message> newList = new ArrayList<>();
        newList.add(message);
        newList.addAll(this.messages);
        setMessages(newList);
    }

    public void markMessageAsRead(String messageId) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getId().equals(messageId)) {
                messages.get(i).setUnread(false);
                notifyItemChanged(i);
                break;
            }
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        TextView contentView;
        TextView timeView;
        View unreadIndicator;

        MessageViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.message_title);
            contentView = itemView.findViewById(R.id.message_content);
            timeView = itemView.findViewById(R.id.message_time);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
        }
    }
} 