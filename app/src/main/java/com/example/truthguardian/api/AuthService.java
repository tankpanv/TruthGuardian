package com.example.truthguardian.api;

import com.example.truthguardian.model.LoginRequest;
import com.example.truthguardian.model.LoginResponse;
import com.example.truthguardian.model.RegisterRequest;
import com.example.truthguardian.model.RegisterResponse;
import com.example.truthguardian.model.UploadResponse;
import com.example.truthguardian.model.UserResponse;
import com.example.truthguardian.model.UserUpdateRequest;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface AuthService {
    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/auth/user")
    Call<UserResponse> getUserInfo(@Header("Authorization") String token);

    @PUT("api/auth/user")
    Call<UserResponse> updateUser(@Header("Authorization") String token, @Body UserUpdateRequest request);

    @Multipart
    @POST("api/upload/local")
    Call<UploadResponse> uploadAvatar(
        @Header("Authorization") String token,
        @Part MultipartBody.Part file
    );
} 