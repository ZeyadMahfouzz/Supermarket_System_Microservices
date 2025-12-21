package com.supermarket.supermarket_system.dto;

import com.supermarket.supermarket_system.dto.cart.ItemDetailsDto;
import java.util.Map;

public class CheckoutEventDto {
    private Long userId;
    private Map<String, ItemDetailsDto> itemDetails;
    private String paymentMethod;
    private Double totalPrice;

    public CheckoutEventDto() {}

    public CheckoutEventDto(Long userId, Map<String, ItemDetailsDto> itemDetails, String paymentMethod, Double totalPrice) {
        this.userId = userId;
        this.itemDetails = itemDetails;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Map<String, ItemDetailsDto> getItemDetails() { return itemDetails; }
    public void setItemDetails(Map<String, ItemDetailsDto> itemDetails) { this.itemDetails = itemDetails; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
}