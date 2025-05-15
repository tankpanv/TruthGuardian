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

public class DebunkArticleListActivity extends AppCompatActivity implements DebunkArticleAdapter.OnArticleClickListener, DebunkArticleAdapter.OnTagClickListener {

    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_TOKEN = "access_token";

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
    private String currentStatusFilter = "published";
    private String currentTags = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debunk_article_list);

        // 初始化视图
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        emptyView = findViewById(R.id.empty_view);
        searchView = findViewById(R.id.search_view);
        fabCreate = findViewById(R.id.fab_create);

        // 设置返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        // 初始化网络服务
        debunkService = ApiClient.getClient().create(DebunkService.class);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DebunkArticleAdapter(articleList, this, this);
        recyclerView.setAdapter(adapter);

        // 设置搜索监听
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchQuery = query;
                currentPage = 1;
                loadArticles(true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText) && !TextUtils.isEmpty(currentSearchQuery)) {
                    currentSearchQuery = "";
                    currentPage = 1;
                    loadArticles(true);
                }
                return true;
            }
        });

        // 设置状态过滤器点击事件
        findViewById(R.id.filter_published).setOnClickListener(v -> filterByStatus("published"));
        findViewById(R.id.filter_draft).setOnClickListener(v -> filterByStatus("draft"));
        findViewById(R.id.filter_archived).setOnClickListener(v -> filterByStatus("archived"));
        findViewById(R.id.filter_all).setOnClickListener(v -> filterByStatus("all"));

        // 设置创建按钮点击事件
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
        loadArticles(true);
    }

    /**
     * 根据状态过滤文章
     */
    private void filterByStatus(String status) {
        if (!status.equals(currentStatusFilter)) {
            currentStatusFilter = status;
            currentPage = 1;
            loadArticles(true);
            
            // 更新过滤器UI
            updateFilterUI(status);
        }
    }
    
    /**
     * 更新过滤器UI
     */
    private void updateFilterUI(String status) {
        TextView filterPublished = findViewById(R.id.filter_published);
        TextView filterDraft = findViewById(R.id.filter_draft);
        TextView filterArchived = findViewById(R.id.filter_archived);
        TextView filterAll = findViewById(R.id.filter_all);
        
        filterPublished.setBackgroundResource(status.equals("published") ? R.drawable.filter_selected_bg : R.drawable.filter_normal_bg);
        filterDraft.setBackgroundResource(status.equals("draft") ? R.drawable.filter_selected_bg : R.drawable.filter_normal_bg);
        filterArchived.setBackgroundResource(status.equals("archived") ? R.drawable.filter_selected_bg : R.drawable.filter_normal_bg);
        filterAll.setBackgroundResource(status.equals("all") ? R.drawable.filter_selected_bg : R.drawable.filter_normal_bg);
    }

    /**
     * 加载文章列表
     */
    private void loadArticles(boolean clearList) {
        isLoading = true;
        showLoading(true);
        
        debunkService.getArticles(currentPage, 10, currentStatusFilter, currentSearchQuery, currentTags)
                .enqueue(new Callback<DebunkArticleListResponse>() {
                    @Override
                    public void onResponse(Call<DebunkArticleListResponse> call, Response<DebunkArticleListResponse> response) {
                        isLoading = false;
                        showLoading(false);
                        
                        if (response.isSuccessful() && response.body() != null 
                                && response.body().getData() != null) {
                            DebunkArticleListResponse.Data data = response.body().getData();
                            
                            if (clearList) {
                                articleList.clear();
                            }
                            
                            if (data.getItems() != null) {
                                articleList.addAll(data.getItems());
                            }
                            
                            adapter.notifyDataSetChanged();
                            
                            if (data.getPagination() != null) {
                                totalPages = data.getPagination().getTotalPages();
                            }
                            
                            showEmptyView(articleList.isEmpty());
                        } else {
                            Toast.makeText(DebunkArticleListActivity.this, R.string.error_load_failed, Toast.LENGTH_SHORT).show();
                            showEmptyView(articleList.isEmpty());
                        }
                    }

                    @Override
                    public void onFailure(Call<DebunkArticleListResponse> call, Throwable t) {
                        isLoading = false;
                        showLoading(false);
                        Toast.makeText(DebunkArticleListActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
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
        loadArticles(false);
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

    // 实现标签点击监听器接口
    @Override
    public void onTagClick(String tag) {
        // 清除搜索栏
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
        
        // 重置状态筛选器
        currentStatusFilter = "all";
        
        // 更新UI状态
        updateFilterUI("all");
        
        // 显示已选标签提示
        Toast.makeText(this, "筛选标签: " + tag, Toast.LENGTH_SHORT).show();
        
        // 过滤符合该标签的文章
        filterArticlesByTag(tag);
    }
    
    private void filterArticlesByTag(String tag) {
        try {
            // 设置当前标签
            currentTags = tag;
            
            // 重置其他筛选条件
            currentPage = 1;
            currentSearchQuery = "";
            
            // 显示加载状态
            isLoading = true;
            showLoading(true);
            
            // 直接使用tags参数进行现有API调用
            loadArticles(true);
            
            /*
            // 以下代码在API不支持tags参数的情况下才需要使用
            // 调用专门的API获取带有该标签的文章
            debunkService.getArticlesByTag(tag, currentPage, 10)
                    .enqueue(new Callback<List<DebunkArticle>>() {
                        @Override
                        public void onResponse(Call<List<DebunkArticle>> call, Response<List<DebunkArticle>> response) {
                            isLoading = false;
                            showLoading(false);
                            
                            if (response.isSuccessful() && response.body() != null) {
                                List<DebunkArticle> fetchedArticles = response.body();
                                articleList.clear();
                                
                                if (!fetchedArticles.isEmpty()) {
                                    articleList.addAll(fetchedArticles);
                                    adapter.notifyDataSetChanged();
                                }
                                
                                showEmptyView(articleList.isEmpty());
                            } else {
                                Toast.makeText(DebunkArticleListActivity.this, 
                                        R.string.error_load_failed, 
                                        Toast.LENGTH_SHORT).show();
                                showEmptyView(articleList.isEmpty());
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<List<DebunkArticle>> call, Throwable t) {
                            isLoading = false;
                            showLoading(false);
                            Toast.makeText(DebunkArticleListActivity.this, 
                                    R.string.error_network, 
                                    Toast.LENGTH_SHORT).show();
                            showEmptyView(articleList.isEmpty());
                        }
                    });
            */
        } catch (Exception e) {
            Toast.makeText(this, "过滤文章出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("DebunkArticleListActivity", "过滤文章出错", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 刷新列表
        loadArticles(true);
    }
} 