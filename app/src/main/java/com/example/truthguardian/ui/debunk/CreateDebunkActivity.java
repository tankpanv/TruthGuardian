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
import com.example.truthguardian.model.DebunkArticleRequest;
import com.example.truthguardian.model.DebunkArticleResponse;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateDebunkActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_TOKEN = "access_token";

    private EditText etTitle;
    private EditText etContent;
    private EditText etSummary;
    private EditText etSource;
    private EditText etTags;
    private ChipGroup chipGroupTags;
    private Button btnSaveDraft;
    private Button btnPublish;
    private ProgressBar progressBar;
    private DebunkService debunkService;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_debunk);

        // 初始化视图
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        etSummary = findViewById(R.id.et_summary);
        etSource = findViewById(R.id.et_source);
        etTags = findViewById(R.id.et_tags);
        chipGroupTags = findViewById(R.id.chip_group_tags);
        btnSaveDraft = findViewById(R.id.btn_save_draft);
        btnPublish = findViewById(R.id.btn_publish);
        progressBar = findViewById(R.id.progress_bar);

        // 设置返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        // 初始化API服务
        debunkService = ApiClient.getClient().create(DebunkService.class);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // 设置标签添加事件
        findViewById(R.id.btn_add_tag).setOnClickListener(v -> addTag());

        // 设置保存草稿按钮点击事件
        btnSaveDraft.setOnClickListener(v -> saveArticle("draft"));

        // 设置发布按钮点击事件
        btnPublish.setOnClickListener(v -> saveArticle("published"));
    }

    /**
     * 添加标签
     */
    private void addTag() {
        String tagText = etTags.getText().toString().trim();
        if (!TextUtils.isEmpty(tagText)) {
            // 创建标签Chip
            Chip chip = new Chip(this);
            chip.setText(tagText);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> chipGroupTags.removeView(chip));
            
            // 添加到ChipGroup
            chipGroupTags.addView(chip);
            
            // 清除输入框
            etTags.setText("");
        }
    }

    /**
     * 保存文章
     */
    private void saveArticle(String status) {
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

        // 如果没有标签，使用默认标签
        if (tags.isEmpty()) {
            tags = Arrays.asList("未分类");
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
                new ArrayList<>(), // 暂时不支持关联谣言报告
                new ArrayList<>()  // 暂时不支持关联辟谣依据
        );

        // 获取Token
        String token = "Bearer " + sharedPreferences.getString(KEY_TOKEN, "");

        // 调用API创建文章
        debunkService.createArticle(token, request)
                .enqueue(new Callback<DebunkArticleResponse>() {
                    @Override
                    public void onResponse(Call<DebunkArticleResponse> call, Response<DebunkArticleResponse> response) {
                        showLoading(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            String message = "发布".equals(status) ? "文章发布成功" : "草稿保存成功";
                            Toast.makeText(CreateDebunkActivity.this, message, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CreateDebunkActivity.this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<DebunkArticleResponse> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(CreateDebunkActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 显示加载中视图
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSaveDraft.setEnabled(!show);
        btnPublish.setEnabled(!show);
    }
} 