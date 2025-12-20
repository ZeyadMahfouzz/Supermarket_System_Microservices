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

    private static final long serialVersionUID = 1L;

    private Long userId;
    private Map<String, Integer> items;              // itemId -> quantity
    private Map<String, Double> itemPrices;          // itemId -> unitPrice
    private Map<String, ItemDetailsDto> itemDetails; // ✅ ADDED: itemId -> full details (name, image, etc.)
    private String paymentMethod;
    private Double totalPrice;
}