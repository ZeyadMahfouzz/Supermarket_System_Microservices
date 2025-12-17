package com.supermarket.supermarket_system.dto.cart;

import java.util.Map;

public class CartCheckoutEvent {
    private Long userId;
    // itemId as String -> quantity
    private Map<String, Integer> items;
    private String paymentMethod;
    private Double totalPrice; // optional

    public CartCheckoutEvent() {}

    public CartCheckoutEvent(Long userId, Map<String, Integer> items, String paymentMethod, Double totalPrice) {
        this.userId = userId;
        this.items = items;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Map<String, Integer> getItems() { return items; }
    public void setItems(Map<String, Integer> items) { this.items = items; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
}
