package com.example.truthguardian.ui.debunk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truthguardian.R;
import com.example.truthguardian.adapter.DebunkArticleAdapter;
import com.example.truthguardian.api.ApiClient;
import com.example.truthguardian.api.DebunkService;
import com.example.truthguardian.model.DebunkArticle;
import com.example.truthguardian.model.DebunkArticleListResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyDebunkArticleListActivity extends AppCompatActivity implements DebunkArticleAdapter.OnArticleClickListener {

    private static final String TAG = "MyDebunkArticleListActivity";
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";

    private RecyclerView recyclerView;
    private DebunkArticleAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout emptyView;
    private SearchView searchView;
    private FloatingActionButton fabCreate;
    private DebunkService debunkService;
    private SharedPreferences sharedPreferences;
    private List<DebunkArticle> articleList = new ArrayList<>();
    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isLoading = false;
    private String currentSearchQuery = "";
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_debunk_article_list);

        // 初始化视图
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        emptyView = findViewById(R.id.empty_view);
        searchView = findViewById(R.id.search_view);
        fabCreate = findViewById(R.id.fab_create);
        
        // 设置标题和返回按钮
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("我的辟谣");
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        // 初始化网络服务
        debunkService = ApiClient.getClient().create(DebunkService.class);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userId = sharedPreferences.getInt(KEY_USER_ID, 0);
        
        // 检查授权状态
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        String token = sharedPreferences.getString(KEY_TOKEN, "");
        Log.d(TAG, "登录状态: " + isLoggedIn + 
              ", 用户ID: " + userId + 
              ", Token存在: " + (token != null && !token.isEmpty() ? "是" : "否") +
              ", Token长度: " + (token != null ? token.length() : 0));
        
        if (!isLoggedIn || token.isEmpty()) {
            Log.e(TAG, "用户未登录或Token为空");
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            // 可以考虑跳转到登录页面
            // Intent intent = new Intent(this, LoginActivity.class);
            // startActivity(intent);
            // finish();
            // return;
        }

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DebunkArticleAdapter(articleList, this);
        recyclerView.setAdapter(adapter);

        // 设置搜索监听
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchQuery = query;
                currentPage = 1;
                loadMyArticles(true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText) && !TextUtils.isEmpty(currentSearchQuery)) {
                    currentSearchQuery = "";
                    currentPage = 1;
                    loadMyArticles(true);
                }
                return true;
            }
        });

        // 设置创建按钮
        fabCreate.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateDebunkArticleActivity.class);
            startActivity(intent);
        });

        // 设置滚动加载更多
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && currentPage < totalPages) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadMoreArticles();
                    }
                }
            }
        });

        // 初始加载数据
        loadMyArticles(true);
    }

    /**
     * 加载我的文章列表
     */
    private void loadMyArticles(boolean clearList) {
        if (!sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        isLoading = true;
        showLoading(true);
        
        // 获取保存的token
        String savedToken = sharedPreferences.getString(KEY_TOKEN, "");
        Log.d(TAG, "SharedPreferences中的Token: " + savedToken);
        
        // 检测token是否已经包含Bearer前缀
        String token;
        if (savedToken.startsWith("Bearer ")) {
            // 如果已经包含Bearer前缀，直接使用
            token = savedToken;
        } else {
            // 如果不包含，添加Bearer前缀
            token = "Bearer " + savedToken;
        }
        
        Log.d(TAG, "使用的Authorization Token: " + token);
        
        // 打印请求参数
        Log.d(TAG, "请求参数: 页码=" + currentPage + ", 每页数量=10, 状态=all, 标签=''");
        
        // 调用新的API获取当前登录用户的文章列表
        Log.d(TAG, "开始请求我的辟谣列表");
        
        Call<DebunkArticleListResponse> call = debunkService.getCurrentUserArticles(token, currentPage, 10, "all", "");
        call.enqueue(new Callback<DebunkArticleListResponse>() {
            @Override
            public void onResponse(Call<DebunkArticleListResponse> call, Response<DebunkArticleListResponse> response) {
                isLoading = false;
                showLoading(false);
                
                Log.d(TAG, "API响应码: " + response.code());
                Log.d(TAG, "请求URL: " + call.request().url());
                Log.d(TAG, "Authorization Header: " + call.request().header("Authorization"));
                
                if (response.isSuccessful() && response.body() != null 
                        && response.body().getData() != null) {
                    DebunkArticleListResponse.Data data = response.body().getData();
                    
                    if (clearList) {
                        articleList.clear();
                    }
                    
                    if (data.getItems() != null) {
                        articleList.addAll(data.getItems());
                        Log.d(TAG, "成功获取当前用户的文章，数量: " + data.getItems().size());
                    } else {
                        Log.d(TAG, "文章列表为空");
                    }
                    
                    adapter.notifyDataSetChanged();
                    
                    if (data.getPagination() != null) {
                        totalPages = data.getPagination().getTotalPages();
                        Log.d(TAG, "总页数: " + totalPages);
                    }
                    
                    showEmptyView(articleList.isEmpty());
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "空错误体";
                        Log.e(TAG, "获取文章失败: HTTP状态码: " + response.code() + 
                                ", 错误信息: " + response.message() + 
                                ", 错误体: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "读取错误体时发生异常", e);
                    }
                    
                    Toast.makeText(MyDebunkArticleListActivity.this, "获取文章失败: " + response.code(), Toast.LENGTH_SHORT).show();
                    showEmptyView(articleList.isEmpty());
                }
            }

            @Override
            public void onFailure(Call<DebunkArticleListResponse> call, Throwable t) {
                isLoading = false;
                showLoading(false);
                Log.e(TAG, "网络请求失败: " + t.getMessage(), t);
                Toast.makeText(MyDebunkArticleListActivity.this, "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyView(articleList.isEmpty());
            }
        });
    }

    /**
     * 加载更多文章
     */
    private void loadMoreArticles() {
        if (isLoading) return;
        
        currentPage++;
        loadMyArticles(false);
    }

    /**
     * 显示加载中视图
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * 显示空视图
     */
    private void showEmptyView(boolean show) {
        emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * 文章点击事件
     */
    @Override
    public void onArticleClick(DebunkArticle article) {
        Intent intent = new Intent(this, DebunkArticleDetailActivity.class);
        intent.putExtra("article_id", article.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 刷新列表
        loadMyArticles(true);
    }
} 