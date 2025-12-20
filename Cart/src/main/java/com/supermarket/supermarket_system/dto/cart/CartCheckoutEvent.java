package com.supermarket.supermarket_system.dto.cart;

import java.io.Serializable;
import java.util.Map;

public class CartCheckoutEvent implements Serializable {
    private Long userId;
    private Map<String, Integer> items;  // itemId -> quantity
    private Map<String, Double> itemPrices;  // itemId -> unitPrice
    private String paymentMethod;
    private Double totalPrice;

    public CartCheckoutEvent() {}

    public CartCheckoutEvent(Long userId, Map<String, Integer> items, Map<String, Double> itemPrices,
                             String paymentMethod, Double totalPrice) {
        this.userId = userId;
        this.items = items;
        this.itemPrices = itemPrices;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Map<String, Integer> getItems() { return items; }
    public void setItems(Map<String, Integer> items) { this.items = items; }

    public Map<String, Double> getItemPrices() { return itemPrices; }
    public void setItemPrices(Map<String, Double> itemPrices) { this.itemPrices = itemPrices; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
}