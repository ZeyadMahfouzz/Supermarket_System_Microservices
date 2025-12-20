package com.supermarket.supermarket_system.dto.payment;

import com.supermarket.supermarket_system.dto.payment.CartItemDto;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CartCheckoutEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, ItemDetailsDto> itemDetails;

    private Long userId;

    // Option 1: Structured items (used by Payment service)
    private List<CartItemDto> itemDtos;

    // Option 2: Lightweight maps (used by Cart / Inventory services)
    private Map<String, Integer> items;       // itemId -> quantity
    private Map<String, Double> itemPrices;   // itemId -> unitPrice

    private String paymentMethod;
    private Double totalPrice;

    public CartCheckoutEvent() {}

    /* Constructor used by Cart service (maps-based) - NOW INCLUDES itemDetails */
    public CartCheckoutEvent(
            Long userId,
            Map<String, Integer> items,
            Map<String, Double> itemPrices,
            Map<String, ItemDetailsDto> itemDetails,
            String paymentMethod,
            Double totalAmount
    ) {
        this.userId = userId;
        this.items = items;
        this.itemPrices = itemPrices;
        this.itemDetails = itemDetails;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalAmount;
    }

    /* Constructor used by Payment service (DTO-based) */
    public CartCheckoutEvent(
            Long userId,
            List<CartItemDto> itemDtos,
            Double totalAmount
    ) {
        this.userId = userId;
        this.itemDtos = itemDtos;
        this.totalPrice = totalAmount;
    }

    // -------- Getters & Setters --------

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartItemDto> getItemDtos() {
        return itemDtos;
    }

    public void setItemDtos(List<CartItemDto> itemDtos) {
        this.itemDtos = itemDtos;
    }

    public Map<String, Integer> getItems() {
        return items;
    }

    public void setItems(Map<String, Integer> items) {
        this.items = items;
    }

    public Map<String, Double> getItemPrices() {
        return itemPrices;
    }

    public void setItemPrices(Map<String, Double> itemPrices) {
        this.itemPrices = itemPrices;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setItemDetails(Map<String, ItemDetailsDto> itemDetails) {
        this.itemDetails = itemDetails;
    }

    public Map<String, ItemDetailsDto> getItemDetails() {
        return itemDetails;
    }

}