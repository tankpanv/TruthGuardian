package com.example.truthguardian.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.example.truthguardian.model.DebunkArticle;
import com.example.truthguardian.model.DebunkArticleResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "http://47.106.74.86:5005/";
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_TOKEN = "access_token";
    
    private static Retrofit retrofit = null;
    private static Context context = null;

    public static void init(Context appContext) {
        context = appContext.getApplicationContext();
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            if (context == null) {
                throw new IllegalStateException("ApiClient must be initialized with context first");
            }

            // 创建HTTP日志拦截器
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 创建认证拦截器
            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    
                    // 如果请求已经包含Authorization header，直接处理
                    if (original.header("Authorization") != null) {
                        return chain.proceed(original);
                    }
                    
                    // 从SharedPreferences获取token
                    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    String token = prefs.getString(KEY_TOKEN, null);
                    
                    // 如果没有token，直接处理
                    if (token == null) {
                        Log.d(TAG, "No token found in SharedPreferences");
                        return chain.proceed(original);
                    }

                    // 确保token格式正确
                    if (!token.startsWith("Bearer ")) {
                        token = "Bearer " + token;
                        Log.d(TAG, "Added Bearer prefix to token");
                    } else {
                        Log.d(TAG, "Token already has Bearer prefix");
                    }

                    Log.d(TAG, "Adding Authorization header with token: " + 
                        (token != null ? token.substring(0, Math.min(token.length(), 20)) + "..." : "null"));

                    // 添加Authorization header
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", token)
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            };

            // 配置OkHttpClient
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(authInterceptor);
            httpClient.addInterceptor(loggingInterceptor);
            httpClient.connectTimeout(90, TimeUnit.SECONDS);
            httpClient.readTimeout(90, TimeUnit.SECONDS);
            httpClient.writeTimeout(90, TimeUnit.SECONDS);

            // 配置GSON
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(DebunkArticleResponse.class, new DebunkArticleResponseDeserializer())
                    .create();

            // 构建Retrofit实例
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
    
    /**
     * 自定义反序列化器，处理API返回的多种可能结构
     */
    private static class DebunkArticleResponseDeserializer implements JsonDeserializer<DebunkArticleResponse> {
        @Override
        public DebunkArticleResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                DebunkArticleResponse response = new DebunkArticleResponse();
                JsonObject jsonObject = json.getAsJsonObject();
                
                // 处理状态、消息和错误码
                if (jsonObject.has("status")) {
                    response.setStatus(jsonObject.get("status").getAsString());
                }
                
                if (jsonObject.has("message")) {
                    response.setMessage(jsonObject.get("message").getAsString());
                }
                
                if (jsonObject.has("error_code")) {
                    response.setErrorCode(jsonObject.get("error_code").getAsString());
                }
                
                // 尝试多种方式解析文章数据
                DebunkArticle article = null;
                
                // 尝试解析article字段
                if (jsonObject.has("article") && !jsonObject.get("article").isJsonNull()) {
                    article = context.deserialize(jsonObject.get("article"), DebunkArticle.class);
                } 
                // 尝试解析整个JSON作为文章（根据日志中看到的响应格式）
                else if (jsonObject.has("id") && jsonObject.has("title")) {
                    article = context.deserialize(jsonObject, DebunkArticle.class);
                }
                
                if (article != null) {
                    response.setArticle(article);
                }
                
                return response;
            } catch (Exception e) {
                android.util.Log.e("ApiClient", "Error deserializing DebunkArticleResponse", e);
                return new Gson().fromJson(json, DebunkArticleResponse.class);
            }
        }
    }
} 