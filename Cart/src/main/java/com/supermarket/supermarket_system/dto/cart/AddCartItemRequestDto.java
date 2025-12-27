//Data Transfer Object for adding items to cart (though the controller doesn't use it yet).
package com.supermarket.supermarket_system.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AddCartItemRequestDto {

    @NotNull
    private Long itemId;

    @Min(1)
    private int quantity;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

}
