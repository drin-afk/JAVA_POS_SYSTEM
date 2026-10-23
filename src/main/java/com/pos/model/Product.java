package com.pos.model;

public class Product {

    private int productId;
    private String productCode;
    private String productName;
    private String category;
    private double price;
    private int quantity;
    private String status;

    public Product(int productId, String productCode, String productName,
                   String category, double price, int quantity, String status) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.status = status;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStatus() {
        return status;
    }
}
