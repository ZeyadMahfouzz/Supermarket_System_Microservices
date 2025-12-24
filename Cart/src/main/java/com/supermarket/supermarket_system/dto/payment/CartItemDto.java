package com.supermarket.supermarket_system.dto.payment;

import java.io.Serializable;

public class CartItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long itemId;
    private int quantity;
    private Double unitPrice;
    private String name;
    private String imageUrl;

    public CartItemDto() {
    }

    public CartItemDto(Long itemId, int quantity, Double unitPrice, String name, String imageUrl) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}