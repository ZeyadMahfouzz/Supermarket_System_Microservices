package com.supermarket.supermarket_system.dto.cart;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartCheckoutEvent implements Serializable {

    private Long userId;
    private Map<String, Integer> items;  // itemId -> quantity
    private String paymentMethod;
    private Double totalPrice;

    // Optional: Add item prices if you want to avoid fetching from Items service
    private Map<String, Double> itemPrices;  // itemId -> unitPrice
}