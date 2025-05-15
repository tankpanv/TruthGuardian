package com.example.truthguardian.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truthguardian.R;
import com.example.truthguardian.model.DebunkArticle;

import java.util.List;

public class DebunkArticleAdapter extends RecyclerView.Adapter<DebunkArticleAdapter.ArticleViewHolder> {

    private List<DebunkArticle> articleList;
    private OnArticleClickListener listener;
    private OnTagClickListener tagListener;

    public interface OnArticleClickListener {
        void onArticleClick(DebunkArticle article);
    }
    
    public interface OnTagClickListener {
        void onTagClick(String tag);
    }

    public DebunkArticleAdapter(List<DebunkArticle> articleList, OnArticleClickListener listener) {
        this.articleList = articleList;
        this.listener = listener;
    }
    
    public DebunkArticleAdapter(List<DebunkArticle> articleList, OnArticleClickListener listener, OnTagClickListener tagListener) {
        this.articleList = articleList;
        this.listener = listener;
        this.tagListener = tagListener;
    }
    
    public void setOnTagClickListener(OnTagClickListener listener) {
        this.tagListener = listener;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_debunk_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        DebunkArticle article = articleList.get(position);
        holder.bind(article, listener, tagListener);
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    static class ArticleViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvSummary;
        TextView tvAuthor;
        TextView tvStatus;
        TextView tvViews;
        TextView tvLikes;
        TextView tvDate;
        TextView tvTags;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_article_title);
            tvSummary = itemView.findViewById(R.id.tv_article_summary);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvViews = itemView.findViewById(R.id.tv_views);
            tvLikes = itemView.findViewById(R.id.tv_likes);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTags = itemView.findViewById(R.id.tv_tags);
        }

        public void bind(DebunkArticle article, OnArticleClickListener listener, OnTagClickListener tagListener) {
            // 设置标题
            tvTitle.setText(article.getTitle());
            
            // 设置摘要
            if (!TextUtils.isEmpty(article.getSummary())) {
                tvSummary.setText(article.getSummary());
                tvSummary.setVisibility(View.VISIBLE);
            } else {
                tvSummary.setVisibility(View.GONE);
            }
            
            // 设置作者
            String authorName = "";
            if (article.getAuthor() != null) {
                authorName = article.getAuthor().getDisplayName();
            }
            tvAuthor.setText(authorName);
            
            // 设置状态
            String status = article.getStatus();
            tvStatus.setText(getStatusText(status));
            tvStatus.setBackgroundColor(getStatusColor(status, tvStatus.getContext().getResources().getColor(R.color.status_default)));
            
            // 设置阅读量和点赞数
            tvViews.setText(String.valueOf(article.getViews()));
            tvLikes.setText(String.valueOf(article.getLikes()));
            
            // 设置日期
            tvDate.setText(article.getPublishedAt() != null ? article.getPublishedAt() : article.getCreatedAt());
            
            // 设置标签
            if (article.getTags() != null && !article.getTags().isEmpty()) {
                StringBuilder tagBuilder = new StringBuilder();
                for (String tag : article.getTags()) {
                    if (tag == null) continue;
                    if (tagBuilder.length() > 0) {
                        tagBuilder.append(" • ");
                    }
                    tagBuilder.append(tag);
                }
                if (tagBuilder.length() > 0) {
                    tvTags.setText(tagBuilder.toString());
                    tvTags.setVisibility(View.VISIBLE);
                    
                    // 设置标签点击监听器
                    if (tagListener != null) {
                        tvTags.setOnClickListener(v -> {
                            if (article.getTags() != null && !article.getTags().isEmpty()) {
                                // 默认使用第一个标签
                                String firstTag = article.getTags().get(0);
                                if (firstTag != null) {
                                    tagListener.onTagClick(firstTag);
                                }
                            }
                        });
                    }
                } else {
                    tvTags.setVisibility(View.GONE);
                }
            } else {
                tvTags.setVisibility(View.GONE);
            }
            
            // 设置点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onArticleClick(article);
                }
            });
        }
        
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
        
        private int getStatusColor(String status, int defaultColor) {
            switch (status) {
                case "published":
                    return 0xFF4CAF50; // 绿色
                case "draft":
                    return 0xFFFF9800; // 橙色
                case "archived":
                    return 0xFF9E9E9E; // 灰色
                default:
                    return defaultColor;
            }
        }
    }
} 