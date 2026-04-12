package com.ecommerce.demo.dto;

import java.util.List;

public class AdminStatsDTO {
    private double totalRevenue;
    private long totalOrders;
    private long totalUsers;
    private long totalProducts;
    private List<TopProductDTO> topProducts;
    private List<LowStockDTO> lowStockItems;

    public AdminStatsDTO() {}

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }
    public List<TopProductDTO> getTopProducts() { return topProducts; }
    public void setTopProducts(List<TopProductDTO> topProducts) { this.topProducts = topProducts; }
    public List<LowStockDTO> getLowStockItems() { return lowStockItems; }
    public void setLowStockItems(List<LowStockDTO> lowStockItems) { this.lowStockItems = lowStockItems; }

    public static class TopProductDTO {
        private String productName;
        private long totalQuantitySold;

        public TopProductDTO(String productName, long totalQuantitySold) {
            this.productName = productName;
            this.totalQuantitySold = totalQuantitySold;
        }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public long getTotalQuantitySold() { return totalQuantitySold; }
        public void setTotalQuantitySold(long totalQuantitySold) { this.totalQuantitySold = totalQuantitySold; }
    }

    public static class LowStockDTO {
        private String productName;
        private String size;
        private int stockQuantity;

        public LowStockDTO(String productName, String size, int stockQuantity) {
            this.productName = productName;
            this.size = size;
            this.stockQuantity = stockQuantity;
        }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }
        public int getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    }
}
