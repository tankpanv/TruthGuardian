package com.example.truthguardian.ui.debunk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.truthguardian.R;
import com.example.truthguardian.api.ApiClient;
import com.example.truthguardian.api.DebunkService;
import com.example.truthguardian.model.DebunkArticleRequest;
import com.example.truthguardian.model.DebunkArticleResponse;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateDebunkArticleActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_TOKEN = "access_token";

    private TextInputEditText titleEditText;
    private TextInputEditText summaryEditText;
    private TextInputEditText sourceEditText;
    private TextInputEditText rumorContentEditText;
    private TextInputEditText clarificationContentEditText;
    private ChipGroup tagsChipGroup;
    private Button submitButton;
    private ProgressBar progressBar;
    private DebunkService debunkService;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_debunk_article);

        // 初始化服务和管理器
        debunkService = ApiClient.getClient().create(DebunkService.class);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // 初始化视图
        initViews();
        setupListeners();
    }

    private void initViews() {
        titleEditText = findViewById(R.id.titleEditText);
        summaryEditText = findViewById(R.id.summaryEditText);
        sourceEditText = findViewById(R.id.sourceEditText);
        rumorContentEditText = findViewById(R.id.rumorContentEditText);
        clarificationContentEditText = findViewById(R.id.clarificationContentEditText);
        tagsChipGroup = findViewById(R.id.tagsChipGroup);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progress_bar);

        // 设置来源默认值为"用户"
        sourceEditText.setText("用户");

        if (progressBar == null) {
            // 如果布局中没有进度条，可以动态创建一个
            progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        submitButton.setOnClickListener(v -> validateAndSubmit());
    }

    private void validateAndSubmit() {
        // 获取输入值
        String title = titleEditText.getText() != null ? titleEditText.getText().toString() : "";
        String summary = summaryEditText.getText() != null ? summaryEditText.getText().toString() : "";
        String source = sourceEditText.getText() != null ? sourceEditText.getText().toString() : "";
        String rumorContent = rumorContentEditText.getText() != null ? rumorContentEditText.getText().toString() : "";
        String clarificationContent = clarificationContentEditText.getText() != null ? clarificationContentEditText.getText().toString() : "";

        // 表单验证
        if (TextUtils.isEmpty(title)) {
            titleEditText.setError("请输入标题");
            return;
        }

        if (TextUtils.isEmpty(summary)) {
            summaryEditText.setError("请输入摘要");
            return;
        }

        // 为来源设置默认值"用户"
        if (TextUtils.isEmpty(source)) {
            source = "用户";
        }

        // 获取选中的标签
        List<String> tags = getSelectedTags();

        // 如果没有选择标签，提示用户
        if (tags.isEmpty()) {
            Toast.makeText(this, "请至少选择一个标签", Toast.LENGTH_SHORT).show();
            return;
        }

        // 组合内容（谣言+辟谣）
        String combinedContent = String.format("【谣言】\n%s\n\n【辟谣】\n%s", rumorContent, clarificationContent);

        // 提交文章
        submitArticle(title, combinedContent, summary, source, tags);
    }

    private List<String> getSelectedTags() {
        List<String> selectedTags = new ArrayList<>();
        for (int i = 0; i < tagsChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) tagsChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedTags.add(chip.getText().toString());
            }
        }
        return selectedTags;
    }

    private void submitArticle(String title, String content, String summary, String source, List<String> tags) {
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

        // 获取Token (修复：不需要添加Bearer前缀，因为拦截器已经处理)
        String token = sharedPreferences.getString(KEY_TOKEN, "");

        // 调用API创建文章
        debunkService.createArticle(token, request)
                .enqueue(new Callback<DebunkArticleResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DebunkArticleResponse> call, @NonNull Response<DebunkArticleResponse> response) {
                        showLoading(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(CreateDebunkArticleActivity.this, "文章提交成功", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CreateDebunkArticleActivity.this, "操作失败，请重试", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DebunkArticleResponse> call, @NonNull Throwable t) {
                        showLoading(false);
                        Toast.makeText(CreateDebunkArticleActivity.this, "网络错误，请检查网络连接", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 显示加载中视图
     */
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (submitButton != null) {
            submitButton.setEnabled(!show);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 