package com.supermarket.supermarket_system.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateCartItemQuantityRequestDto {

    @NotNull(message = "Cart item ID is required")
    private Long cartItemId;

    @Min(1)
    private int quantity;

    public Long getCartItemId() { return cartItemId; }
    public void setCartItemId(Long cartItemId) { this.cartItemId = cartItemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}