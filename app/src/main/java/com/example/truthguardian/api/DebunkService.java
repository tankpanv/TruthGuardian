package com.example.truthguardian.api;

import com.example.truthguardian.model.DebunkArticle;
import com.example.truthguardian.model.DebunkArticleListResponse;
import com.example.truthguardian.model.DebunkArticleRequest;
import com.example.truthguardian.model.DebunkArticleResponse;
import com.example.truthguardian.model.DebunkArticleStatusRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DebunkService {
    // 获取辟谣文章列表 - 确保路径和后端API一致
    @GET("api/debunk/articles")
    Call<DebunkArticleListResponse> getArticles(
            @Query("page") int page,
            @Query("per_page") int perPage,
            @Query("status") String status,
            @Query("search") String search,
            @Query("tags") String tags
    );
    
    // 获取特定用户的辟谣文章列表
    @GET("api/debunk/articles")
    Call<DebunkArticleListResponse> getUserArticles(
            @Query("author_id") int authorId,
            @Query("page") int page,
            @Query("per_page") int perPage,
            @Query("status") String status,
            @Query("search") String search
    );
    
    // 获取当前登录用户的辟谣文章列表（新接口）
    @GET("api/debunk/user/articles")
    Call<DebunkArticleListResponse> getCurrentUserArticles(
            @Header("Authorization") String token,
            @Query("page") int page,
            @Query("per_page") int perPage,
            @Query("status") String status,
            @Query("tags") String tags
    );

    // 根据标签获取文章
    @GET("api/debunk/articles")
    Call<List<DebunkArticle>> getArticlesByTag(
            @Query("tag") String tag,
            @Query("page") int page,
            @Query("size") int size
    );

    // 获取辟谣文章详情
    @GET("api/debunk/articles/{id}")
    Call<DebunkArticleResponse> getArticleDetail(@Path("id") int id);

    // 创建辟谣文章
    @POST("api/debunk/articles")
    Call<DebunkArticleResponse> createArticle(
            @Header("Authorization") String token,
            @Body DebunkArticleRequest request
    );

    // 更新辟谣文章
    @PUT("api/debunk/articles/{id}")
    Call<DebunkArticleResponse> updateArticle(
            @Path("id") int id,
            @Header("Authorization") String token,
            @Body DebunkArticleRequest request
    );

    // 更新辟谣文章状态
    @PATCH("api/debunk/articles/{id}/status")
    Call<DebunkArticleResponse> updateArticleStatus(
            @Path("id") int id,
            @Header("Authorization") String token,
            @Body DebunkArticleStatusRequest request
    );

    // 删除辟谣文章
    @DELETE("api/debunk/articles/{id}")
    Call<DebunkArticleResponse> deleteArticle(
            @Path("id") int id,
            @Header("Authorization") String token
    );
} 