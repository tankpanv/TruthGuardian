package com.example.truthguardian.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DebunkArticleListResponse {
    @SerializedName("data")
    private Data data;

    public Data getData() {
        return data;
    }
    
    // 为保持兼容性，添加直接获取文章的方法
    public List<DebunkArticle> getArticles() {
        if (data != null) {
            return data.getItems();
        }
        return null;
    }
    
    // 为保持兼容性，添加获取总页数的方法
    public int getTotalPages() {
        if (data != null) {
            return data.getPages();
        }
        return 1;
    }

    public static class Data {
        @SerializedName("items")
        private List<DebunkArticle> items;
        
        @SerializedName("page")
        private int page;
        
        @SerializedName("pages")
        private int pages;
        
        @SerializedName("per_page")
        private int perPage;
        
        @SerializedName("total")
        private int total;

        public List<DebunkArticle> getItems() {
            return items;
        }
        
        public int getPage() {
            return page;
        }
        
        public int getPages() {
            return pages;
        }
        
        public int getPerPage() {
            return perPage;
        }
        
        public int getTotal() {
            return total;
        }
        
        // 添加Pagination类的等效方法
        public Pagination getPagination() {
            Pagination pagination = new Pagination();
            pagination.setCurrentPage(page);
            pagination.setTotalPages(pages);
            pagination.setItemsPerPage(perPage);
            pagination.setTotalItems(total);
            return pagination;
        }
    }
    
    // 保留兼容性
    public static class Pagination {
        private int currentPage;
        private int totalPages;
        private int itemsPerPage;
        private int totalItems;
        
        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }
        
        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
        
        public void setItemsPerPage(int itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
        }
        
        public void setTotalItems(int totalItems) {
            this.totalItems = totalItems;
        }
        
        public int getCurrentPage() {
            return currentPage;
        }
        
        public int getTotalPages() {
            return totalPages;
        }
        
        public int getItemsPerPage() {
            return itemsPerPage;
        }
        
        public int getTotalItems() {
            return totalItems;
        }
    }
} 