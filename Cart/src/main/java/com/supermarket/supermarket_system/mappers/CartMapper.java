package com.supermarket.supermarket_system.mappers;

import com.supermarket.supermarket_system.dto.cart.CartItemResponseDto;
import com.supermarket.supermarket_system.dto.cart.CartResponseDto;
import com.supermarket.supermarket_system.models.Cart;
import com.supermarket.supermarket_system.models.CartItem;

import java.util.stream.Collectors;

public class CartMapper {

    public static CartResponseDto toDto(Cart cart) {
        CartResponseDto dto = new CartResponseDto();
        dto.setCartId(cart.getId());
        dto.setUserId(cart.getUserId());
        dto.setItems(cart.getItems().stream().map(CartMapper::toDto).collect(Collectors.toList()));
        dto.setTotalPrice(cart.getTotalPrice());
        return dto;
    }

    private static CartItemResponseDto toDto(CartItem item) {
        CartItemResponseDto dto = new CartItemResponseDto();
        dto.setCartItemId(item.getId());
        dto.setItemId(item.getItemId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }
}
