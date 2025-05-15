package com.example.truthguardian.ui.debunk;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truthguardian.R;
import com.example.truthguardian.api.ApiClient;
import com.example.truthguardian.api.DebunkService;
import com.example.truthguardian.model.DebunkArticle;
import com.example.truthguardian.model.DebunkArticleResponse;
import com.example.truthguardian.model.DebunkArticleStatusRequest;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DebunkArticleDetailActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_TOKEN = "access_token";

    private TextView tvTitle;
    private TextView tvAuthor;
    private TextView tvDate;
    private TextView tvViews;
    private TextView tvLikes;
    private TextView tvStatus;
    private TextView tvSummary;
    private TextView tvContent;
    private TextView tvSource;
    private ChipGroup chipGroupTags;
    private RecyclerView rvRumorReports;
    private RecyclerView rvClarificationReports;
    private RecyclerView rvComments;
    private LinearLayout noCommentsView;
    private Button btnEdit;
    private ImageButton btnLike;
    private ProgressBar progressBar;
    private DebunkService debunkService;
    private SharedPreferences sharedPreferences;
    private int articleId;
    private DebunkArticle article;

    // 添加标志位，避免重复加载
    private boolean isLoading = false;
    private boolean isFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debunk_article_detail);

        // 获取文章ID
        articleId = getIntent().getIntExtra("article_id", -1);
        if (articleId == -1) {
            Toast.makeText(this, "无效的文章ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化视图
        tvTitle = findViewById(R.id.tv_title);
        tvAuthor = findViewById(R.id.tv_author);
        tvDate = findViewById(R.id.tv_date);
        tvViews = findViewById(R.id.tv_views);
        tvLikes = findViewById(R.id.tv_likes);
        tvStatus = findViewById(R.id.tv_status);
        tvSummary = findViewById(R.id.tv_summary);
        tvContent = findViewById(R.id.tv_content);
        tvSource = findViewById(R.id.tv_source);
        chipGroupTags = findViewById(R.id.chip_group_tags);

        rvComments = findViewById(R.id.rv_comments);
        noCommentsView = findViewById(R.id.no_comments_view);
        btnEdit = findViewById(R.id.btn_edit);
        btnLike = findViewById(R.id.btn_like);
        progressBar = findViewById(R.id.progress_bar);

        // 设置返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        // 初始化API服务
        debunkService = ApiClient.getClient().create(DebunkService.class);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // 设置编辑按钮点击事件
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditDebunkActivity.class);
            intent.putExtra("article_id", articleId);
            startActivity(intent);
        });

        // 设置点赞按钮点击事件
        btnLike.setOnClickListener(v -> {
            Toast.makeText(this, "点赞功能开发中", Toast.LENGTH_SHORT).show();
        });

        // 加载文章数据
        loadArticleDetail();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_article_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_archive) {
            updateArticleStatus("archived");
            return true;
        } else if (id == R.id.action_delete) {
            showDeleteConfirmDialog();
            return true;
        } else if (id == R.id.action_share) {
            shareArticle();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    /**
     * 加载文章详情
     */
    private void loadArticleDetail() {
        // 避免重复请求
        if (isLoading) {
            return;
        }
        
        isLoading = true;
        showLoading(true);
        
        debunkService.getArticleDetail(articleId)
                .enqueue(new Callback<DebunkArticleResponse>() {
                    @Override
                    public void onResponse(Call<DebunkArticleResponse> call, Response<DebunkArticleResponse> response) {
                        isLoading = false;
                        showLoading(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            // 检查响应中的article字段
                            DebunkArticle responseArticle = response.body().getArticle();
                            if (responseArticle != null) {
                                article = responseArticle;
                                updateUI(article);
                            } else {
                                // 尝试通过getData方法获取
                                responseArticle = response.body().getData();
                                if (responseArticle != null) {
                                    article = responseArticle;
                                    updateUI(article);
                                } else {
                                    // 输出调试信息
                                    Toast.makeText(DebunkArticleDetailActivity.this, "API响应格式异常，请检查日志", Toast.LENGTH_SHORT).show();
                                    // 尝试手动解析并处理
                                    try {
                                        String responseBody = response.raw().body() != null 
                                                ? response.raw().body().toString() : "null body";
                                        android.util.Log.e("DebunkDetail", "Response body: " + responseBody);
                                    } catch (Exception e) {
                                        android.util.Log.e("DebunkDetail", "Error logging response", e);
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(DebunkArticleDetailActivity.this, R.string.error_load_failed, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<DebunkArticleResponse> call, Throwable t) {
                        isLoading = false;
                        showLoading(false);
                        Toast.makeText(DebunkArticleDetailActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 更新文章状态
     */
    private void updateArticleStatus(String status) {
        if (!isLoggedIn()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        DebunkArticleStatusRequest request = new DebunkArticleStatusRequest(status);
        
        // 获取Token (修复：不需要添加Bearer前缀，因为拦截器已经处理)
        String token = sharedPreferences.getString(KEY_TOKEN, "");
        
        debunkService.updateArticleStatus(articleId, token, request)
                .enqueue(new Callback<DebunkArticleResponse>() {
                    @Override
                    public void onResponse(Call<DebunkArticleResponse> call, Response<DebunkArticleResponse> response) {
                        showLoading(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(DebunkArticleDetailActivity.this, R.string.operation_success, Toast.LENGTH_SHORT).show();
                            // 重新加载文章详情
                            loadArticleDetail();
                        } else {
                            Toast.makeText(DebunkArticleDetailActivity.this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<DebunkArticleResponse> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(DebunkArticleDetailActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 显示删除确认对话框
     */
    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("删除文章")
                .setMessage("确定要删除这篇文章吗？此操作不可撤销。")
                .setPositiveButton(R.string.confirm, (dialog, which) -> deleteArticle())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * 删除文章
     */
    private void deleteArticle() {
        if (!isLoggedIn()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        // 获取Token (修复：不需要添加Bearer前缀，因为拦截器已经处理)
        String token = sharedPreferences.getString(KEY_TOKEN, "");
        
        debunkService.deleteArticle(articleId, token)
                .enqueue(new Callback<DebunkArticleResponse>() {
                    @Override
                    public void onResponse(Call<DebunkArticleResponse> call, Response<DebunkArticleResponse> response) {
                        showLoading(false);
                        
                        if (response.isSuccessful()) {
                            Toast.makeText(DebunkArticleDetailActivity.this, "文章已删除", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(DebunkArticleDetailActivity.this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<DebunkArticleResponse> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(DebunkArticleDetailActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 分享文章
     */
    private void shareArticle() {
        if (article == null) return;
        
        String shareText = article.getTitle() + "\n\n" + article.getSummary() + "\n\n来自「TruthGuardian者」";
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "分享辟谣文章");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    /**
     * 更新UI显示文章详情
     */
    private void updateUI(DebunkArticle article) {
        if (article == null) {
            Toast.makeText(this, "文章数据为空", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 设置标题
        tvTitle.setText(article.getTitle() != null ? article.getTitle() : "");
        
        // 设置作者 - 安全地访问嵌套对象
        if (article.getAuthor() != null) {
            tvAuthor.setText(article.getAuthor().getDisplayName());
        } else {
            tvAuthor.setText("匿名");
        }
        
        // 设置日期
        tvDate.setText(article.getCreatedAt() != null ? article.getCreatedAt() : "");
        
        // 设置浏览量和点赞数
        tvViews.setText(String.valueOf(article.getViews()));
        tvLikes.setText(String.valueOf(article.getLikes()));
        
        // 设置状态
        String status = article.getStatus();
        tvStatus.setText(getStatusText(status));
        tvStatus.setTextColor(getStatusColor(status));
        
        // 设置摘要和内容
        tvSummary.setText(article.getSummary() != null ? article.getSummary() : "");
        tvContent.setText(article.getContent() != null ? article.getContent() : "");
        
        // 设置来源
        if (article.getSource() != null && !article.getSource().isEmpty()) {
            tvSource.setText(article.getSource());
            findViewById(R.id.source_container).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.source_container).setVisibility(View.GONE);
        }
        
        // 设置标签
        setupTags(article);
        
        // 设置评论
        setupComments(article);
        
        // 设置谣言报告和辟谣素材
        // TODO: 实现谣言报告和辟谣素材适配器
        
        // 显示或隐藏编辑按钮
        setupEditButton(article);
    }
    
    /**
     * 设置标签
     */
    private void setupTags(DebunkArticle article) {
        chipGroupTags.removeAllViews();
        List<String> tags = article.getTags();
        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                if (tag != null) {
                    Chip chip = new Chip(this);
                    chip.setText(tag);
                    chipGroupTags.addView(chip);
                }
            }
            chipGroupTags.setVisibility(View.VISIBLE);
        } else {
            chipGroupTags.setVisibility(View.GONE);
        }
    }
    
    /**
     * 设置评论
     */
    private void setupComments(DebunkArticle article) {
        List<DebunkArticle.Comment> comments = article.getComments();
        if (comments != null && !comments.isEmpty()) {
            // TODO: 设置评论适配器
            rvComments.setVisibility(View.VISIBLE);
            noCommentsView.setVisibility(View.GONE);
        } else {
            rvComments.setVisibility(View.GONE);
            noCommentsView.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * 设置编辑按钮
     */
    private void setupEditButton(DebunkArticle article) {
        try {
            // 获取用户ID，处理可能的类型不匹配
            String userIdStr;
            Object userId = sharedPreferences.getAll().get("user_id");
            
            if (userId instanceof String) {
                userIdStr = (String) userId;
            } else if (userId instanceof Integer) {
                userIdStr = String.valueOf(userId);
            } else {
                userIdStr = "";
                Log.e("DebunkArticleDetail", "用户ID类型异常: " + (userId != null ? userId.getClass().getName() : "null"));
            }
            
            // 检查是否为作者
            boolean isAuthor = isLoggedIn() && article.getAuthor() != null && 
                    article.getAuthor().getId() > 0 &&
                    Objects.equals(userIdStr, String.valueOf(article.getAuthor().getId()));
            
            btnEdit.setVisibility(isAuthor ? View.VISIBLE : View.GONE);
            Log.d("DebunkArticleDetail", "用户ID: " + userIdStr + ", 文章作者ID: " + 
                    (article.getAuthor() != null ? article.getAuthor().getId() : "null") + 
                    ", 是否为作者: " + isAuthor);
                    
        } catch (Exception e) {
            Log.e("DebunkArticleDetail", "设置编辑按钮时出错", e);
            btnEdit.setVisibility(View.GONE);
        }
    }

    /**
     * 检查用户是否已登录
     */
    private boolean isLoggedIn() {
    
        String token = sharedPreferences.getString(KEY_TOKEN, null);
        return token != null;
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(String status) {
        switch (status) {
            case "published":
                return "已发布";
            case "draft":
                return "草稿";
            case "archived":
                return "已归档";
            default:
                return status;
        }
    }

    /**
     * 获取状态颜色
     */
    private int getStatusColor(String status) {
        switch (status) {
            case "published":
                return 0xFF4CAF50; // 绿色
            case "draft":
                return 0xFFFF9800; // 橙色
            case "archived":
                return 0xFF9E9E9E; // 灰色
            default:
                return 0xFF000000; // 黑色
        }
    }

    /**
     * 显示加载中视图
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次从其他Activity返回时都刷新文章数据
        // 这样在编辑完成后返回时，可以显示最新的文章内容
        loadArticleDetail();
    }
} 