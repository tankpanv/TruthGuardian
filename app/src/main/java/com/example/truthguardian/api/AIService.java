package com.example.truthguardian.api;

import com.example.truthguardian.model.AIChatRequest;
import com.example.truthguardian.model.AIChatResponse;
import com.example.truthguardian.model.VisualizationResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AIService {
    
    @POST("/api/chat/completions")
    Call<AIChatResponse> getChatResponse(
            @Header("Authorization") String token,
            @Body AIChatRequest request
    );
    
    @GET("/api/visualization/rumor-analysis")
    Call<VisualizationResponse> getVisualizationData(
            @Header("Authorization") String token
    );
} 