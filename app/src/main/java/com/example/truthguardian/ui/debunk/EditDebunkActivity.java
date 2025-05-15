package com.example.truthguardian.ui.debunk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.truthguardian.R;
import com.example.truthguardian.api.ApiClient;
import com.example.truthguardian.api.DebunkService;
import com.example.truthguardian.model.DebunkArticle;
import com.example.truthguardian.model.DebunkArticleRequest;
import com.example.truthguardian.model.DebunkArticleResponse;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditDebunkActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_TOKEN = "access_token";

    private EditText etTitle;
    private EditText etContent;
    private EditText etSummary;
    private EditText etSource;
    private EditText etTags;
    private ChipGroup chipGroupTags;
    private Button btnSaveEdit;
    private Button btnPublish;
    private ProgressBar progressBar;
    private DebunkService debunkService;
    private SharedPreferences sharedPreferences;
    private int articleId;
    private DebunkArticle article;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_debunk);  // 复用创建页面的布局

        // 获取文章ID
        articleId = getIntent().getIntExtra("article_id", -1);
        if (articleId == -1) {
            Toast.makeText(this, "无效的文章ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化视图
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        etSummary = findViewById(R.id.et_summary);
        etSource = findViewById(R.id.et_source);
        etTags = findViewById(R.id.et_tags);
        chipGroupTags = findViewById(R.id.chip_group_tags);
        btnSaveEdit = findViewById(R.id.btn_save_draft);
        btnPublish = findViewById(R.id.btn_publish);
        progressBar = findViewById(R.id.progress_bar);

        // 设置标题
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        // 初始化API服务
        debunkService = ApiClient.getClient().create(DebunkService.class);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // 设置标签添加事件
        findViewById(R.id.btn_add_tag).setOnClickListener(v -> addTag());

        // 设置保存按钮点击事件
        btnSaveEdit.setText("保存修改");
        btnSaveEdit.setOnClickListener(v -> updateArticle("draft"));

        // 设置发布按钮点击事件
        btnPublish.setText("发布修改");
        btnPublish.setOnClickListener(v -> updateArticle("published"));

        // 加载文章数据
        loadArticleDetail();
    }

    /**
     * 加载文章详情
     */
    private void loadArticleDetail() {
        showLoading(true);

        debunkService.getArticleDetail(articleId)
                .enqueue(new Callback<DebunkArticleResponse>() {
                    @Override
                    public void onResponse(Call<DebunkArticleResponse> call, Response<DebunkArticleResponse> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            article = response.body().getData();
                            populateFields(article);
                        } else {
                            Toast.makeText(EditDebunkActivity.this, R.string.error_load_failed, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<DebunkArticleResponse> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(EditDebunkActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 填充表单数据
     */
    private void populateFields(DebunkArticle article) {
        etTitle.setText(article.getTitle());
        etContent.setText(article.getContent());
        etSummary.setText(article.getSummary());
        etSource.setText(article.getSource());

        // 设置标签
        chipGroupTags.removeAllViews();
        if (article.getTags() != null) {
            for (String tag : article.getTags()) {
                addChip(tag);
            }
        }
    }

    /**
     * 添加标签
     */
    private void addTag() {
        String tagText = etTags.getText().toString().trim();
        if (!TextUtils.isEmpty(tagText)) {
            addChip(tagText);
            etTags.setText("");
        }
    }

    /**
     * 添加标签Chip
     */
    private void addChip(String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> chipGroupTags.removeView(chip));
        chipGroupTags.addView(chip);
    }

    /**
     * 更新文章
     */
    private void updateArticle(String status) {
        // 验证输入
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String summary = etSummary.getText().toString().trim();
        String source = etSource.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("请输入标题");
            return;
        }

        if (TextUtils.isEmpty(content)) {
            etContent.setError("请输入内容");
            return;
        }

        if (TextUtils.isEmpty(summary)) {
            etSummary.setError("请输入摘要");
            return;
        }

        // 获取标签列表
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < chipGroupTags.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupTags.getChildAt(i);
            tags.add(chip.getText().toString());
        }

        // 显示加载中
        showLoading(true);

        // 创建请求对象
        DebunkArticleRequest request = new DebunkArticleRequest(
                title,
                content,
                summary,
                source,
                tags,
                new ArrayList<>(),
                new ArrayList<>()
        );

        // 获取Token (修复：不需要添加Bearer前缀，因为拦截器已经处理)
        String token = sharedPreferences.getString(KEY_TOKEN, "");

        // 调用API更新文章
        debunkService.updateArticle(articleId, token, request)
                .enqueue(new Callback<DebunkArticleResponse>() {
                    @Override
                    public void onResponse(Call<DebunkArticleResponse> call, Response<DebunkArticleResponse> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(EditDebunkActivity.this, "文章更新成功", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditDebunkActivity.this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<DebunkArticleResponse> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(EditDebunkActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 显示加载中视图
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSaveEdit.setEnabled(!show);
        btnPublish.setEnabled(!show);
    }
} 