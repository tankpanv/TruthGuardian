package com.example.truthguardian.ui.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truthguardian.R;
import com.example.truthguardian.model.AIChatMessage;

import java.util.List;

public class AIChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    
    private List<AIChatMessage> messages;
    
    public AIChatAdapter(List<AIChatMessage> messages) {
        this.messages = messages;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AIChatMessage message = messages.get(position);
        
        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    @Override
    public int getItemViewType(int position) {
        AIChatMessage message = messages.get(position);
        if (message.getType() == AIChatMessage.TYPE_SENT) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }
    
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        
        SentMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textViewMessage);
        }
        
        void bind(AIChatMessage message) {
            messageText.setText(message.getContent());
        }
    }
    
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        
        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textViewMessage);
        }
        
        void bind(AIChatMessage message) {
            messageText.setText(message.getContent());
        }
    }
} 