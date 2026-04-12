package com.ecommerce.demo.dto;

public class WishlistDTO {
    private Long wishlistId;
    private Long productId;
    private String productName;
    private String description;
    private double price;
    private String category;

    public WishlistDTO(Long wishlistId, Long productId, String productName, String description, double price, String category) {
        this.wishlistId = wishlistId;
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    public Long getWishlistId() { return wishlistId; }
    public void setWishlistId(Long wishlistId) { this.wishlistId = wishlistId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
