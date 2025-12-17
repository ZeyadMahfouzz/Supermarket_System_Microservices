package com.supermarket.supermarket_system.dto.cart;

import jakarta.validation.constraints.Min;

public class UpdateCartItemQuantityRequestDto {

    @Min(1)
    private int quantity;

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
