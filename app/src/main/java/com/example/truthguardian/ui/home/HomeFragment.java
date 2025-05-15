package com.example.truthguardian.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truthguardian.R;
import com.example.truthguardian.adapter.DebunkArticleAdapter;
import com.example.truthguardian.api.ApiClient;
import com.example.truthguardian.api.DebunkService;
import com.example.truthguardian.databinding.FragmentHomeBinding;
import com.example.truthguardian.model.DebunkArticle;
import com.example.truthguardian.model.DebunkArticleListResponse;
import com.example.truthguardian.ui.debunk.CreateDebunkActivity;
import com.example.truthguardian.ui.debunk.DebunkArticleDetailActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements DebunkArticleAdapter.OnArticleClickListener {

    private FragmentHomeBinding binding;
    private List<DebunkArticle> articleList;
    private DebunkArticleAdapter adapter;
    private DebunkService debunkService;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int totalPages = 1;
    private static final int PAGE_SIZE = 10;
    private String currentSearchQuery = "";
    private String currentTag = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 设置浮动按钮点击事件
        binding.fabAdd.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), CreateDebunkActivity.class);
            startActivity(intent);
        });

        // 初始化文章列表
        articleList = new ArrayList<>();

        // 初始化RecyclerView
        RecyclerView recyclerView = binding.rumorsRecyclerView;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // 初始化适配器
        adapter = new DebunkArticleAdapter(articleList, this);
        recyclerView.setAdapter(adapter);

        // 初始化API服务
        debunkService = ApiClient.getClient().create(DebunkService.class);

        // 设置搜索功能
        EditText searchEditText = binding.searchEditText;
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(v.getText().toString());
                return true;
            }
            return false;
        });

        // 设置搜索框文本变化监听
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateClearButtonVisibility();
            }
        });

        // 设置取消按钮点击事件
        binding.btnClear.setOnClickListener(v -> {
            clearSearchAndTags();
        });

        // 初始化标签
        initializeTags();

        // 添加滚动监听器实现无限滚动
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        loadMoreArticles();
                    }
                }
            }
        });

        // 加载第一页数据
        loadArticles();

        return root;
    }

    private void clearSearchAndTags() {
        // 清空搜索框
        binding.searchEditText.setText("");
        
        // 取消标签选中状态
        ChipGroup chipGroup = binding.tagChipGroup;
        // 找到"全部"标签并选中它
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.getText().toString().equals("全部")) {
                chip.setChecked(true);
                break;
            }
        }
        
        // 重置搜索和标签状态
        currentSearchQuery = "";
        currentTag = "";
        
        // 更新取消按钮可见性
        updateClearButtonVisibility();
        
        // 重新加载数据
        resetAndReload();
    }

    private void updateClearButtonVisibility() {
        boolean shouldShowClear = !binding.searchEditText.getText().toString().isEmpty() || 
                                !currentTag.isEmpty();
        binding.btnClear.setVisibility(shouldShowClear ? View.VISIBLE : View.GONE);
    }

    private void initializeTags() {
        ChipGroup chipGroup = binding.tagChipGroup;
        // 定义标签颜色
        int[][] tagColors = {
            {0xFFF0F0F0, 0xFF333333}, // 全部: 浅灰背景，深灰文字
            {0xFFE8F5E9, 0xFF2E7D32}, // 健康: 浅绿背景，深绿文字
            {0xFFE3F2FD, 0xFF1976D2}, // 科技: 浅蓝背景，深蓝文字
            {0xFFFFF3E0, 0xFFF57C00}, // 社会: 浅橙背景，深橙文字
            {0xFFF3E5F5, 0xFF7B1FA2}, // 政治: 浅紫背景，深紫文字
            {0xFFFFEBEE, 0xFFC62828}  // 经济: 浅红背景，深红文字
        };
        List<String> tags = Arrays.asList("全部", "健康", "科技", "社会", "政治", "经济");

        for (int i = 0; i < tags.size(); i++) {
            Chip chip = new Chip(requireContext());
            chip.setText(tags.get(i));
            chip.setCheckable(true);
            chip.setChecked(tags.get(i).equals("全部"));
            
            // 设置标签样式
            chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(tagColors[i][0]));
            chip.setTextColor(tagColors[i][1]);
            chip.setChipStrokeWidth(0);
            chip.setTextSize(14);
            chip.setMinHeight(40);
            
            // 设置选中状态的样式
            int backgroundColor = tagColors[i][0];
            int textColor = tagColors[i][1];
            int selectedBackgroundColor = adjustAlpha(backgroundColor, 0.7f);
            
            android.content.res.ColorStateList backgroundColorStateList = new android.content.res.ColorStateList(
                new int[][]{
                    new int[]{android.R.attr.state_checked},
                    new int[]{}
                },
                new int[]{
                    selectedBackgroundColor,
                    backgroundColor
                }
            );
            
            android.content.res.ColorStateList textColorStateList = new android.content.res.ColorStateList(
                new int[][]{
                    new int[]{android.R.attr.state_checked},
                    new int[]{}
                },
                new int[]{
                    textColor,
                    textColor
                }
            );
            
            chip.setChipBackgroundColor(backgroundColorStateList);
            chip.setTextColor(textColorStateList);
            
            // 设置内边距
            chip.setChipStartPadding(12);
            chip.setChipEndPadding(12);
            
            chipGroup.addView(chip);
        }

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                currentTag = chip.getText().toString().equals("全部") ? "" : chip.getText().toString();
                updateClearButtonVisibility();
                resetAndReload();
            }
        });
    }

    // 调整颜色透明度的辅助方法
    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(android.graphics.Color.alpha(color) * factor);
        int red = android.graphics.Color.red(color);
        int green = android.graphics.Color.green(color);
        int blue = android.graphics.Color.blue(color);
        return android.graphics.Color.argb(alpha, red, green, blue);
    }

    private void performSearch(String query) {
        currentSearchQuery = query;
        updateClearButtonVisibility();
        resetAndReload();
    }

    private void resetAndReload() {
        currentPage = 1;
        isLastPage = false;
        articleList.clear();
        adapter.notifyDataSetChanged();
        loadArticles();
    }

    private void loadArticles() {
        isLoading = true;
        showLoading(true);

        debunkService.getArticles(currentPage, PAGE_SIZE, "published", currentSearchQuery, currentTag)
                .enqueue(new Callback<DebunkArticleListResponse>() {
                    @Override
                    public void onResponse(Call<DebunkArticleListResponse> call, Response<DebunkArticleListResponse> response) {
                        isLoading = false;
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            List<DebunkArticle> articles = response.body().getArticles();
                            if (articles != null) {
                                if (currentPage == 1) {
                                    articleList.clear();
                                }

                                articleList.addAll(articles);
                                adapter.notifyDataSetChanged();

                                totalPages = response.body().getTotalPages();
                                isLastPage = currentPage >= totalPages;

                                // 如果没有文章，显示空视图
                                if (articleList.isEmpty()) {
                                    showEmptyView(true);
                                } else {
                                    showEmptyView(false);
                                }
                            }
                        } else {
                            Toast.makeText(getContext(), "加载失败，请重试", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<DebunkArticleListResponse> call, Throwable t) {
                        isLoading = false;
                        showLoading(false);
                        Toast.makeText(getContext(), "网络错误，请检查网络连接", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMoreArticles() {
        if (isLoading || isLastPage) return;
        currentPage++;
        loadArticles();
    }

    private void showLoading(boolean show) {
        if (binding != null) {
            binding.loadingProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmptyView(boolean show) {
        if (binding != null) {
            binding.emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
            binding.rumorsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onArticleClick(DebunkArticle article) {
        Intent intent = new Intent(getActivity(), DebunkArticleDetailActivity.class);
        intent.putExtra("article_id", article.getId());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}