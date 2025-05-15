package com.example.truthguardian.api;

import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface VisualizationService {
    @GET("api/visualization/rumor-analysis")
    Call<JSONObject> getRumorAnalysis(@Header("Authorization") String token);
} 