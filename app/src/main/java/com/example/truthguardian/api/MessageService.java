package com.example.truthguardian.api;

import com.example.truthguardian.ui.message.Message;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

import java.util.List;

public interface MessageService {
    @GET("api/im/messages/history")
    Call<HistoryResponse> getHistoryMessages(
        @Header("Authorization") String token,
        @Query("direction") String direction,
        @Query("page") int page,
        @Query("per_page") int perPage
    );

    // 历史消息响应结构
    class HistoryResponse {
        public boolean success;
        public String message;
        public Data data;
        public static class Data {
            public List<Message> messages;
            public Message message;
        }
    }
} 