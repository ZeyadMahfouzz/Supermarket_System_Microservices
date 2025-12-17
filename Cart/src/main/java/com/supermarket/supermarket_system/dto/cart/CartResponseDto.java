package com.supermarket.supermarket_system.dto.cart;

import java.util.List;

public class CartResponseDto {
    private Long cartId;
    private Long userId;
    private List<CartItemResponseDto> items;
    private Double totalPrice;

    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<CartItemResponseDto> getItems() { return items; }
    public void setItems(List<CartItemResponseDto> items) { this.items = items; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
}
