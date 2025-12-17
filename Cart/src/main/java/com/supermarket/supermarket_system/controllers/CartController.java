package com.supermarket.supermarket_system.controllers;

import com.supermarket.supermarket_system.dto.cart.AddCartItemRequestDto;
import com.supermarket.supermarket_system.dto.cart.CartResponseDto;
import com.supermarket.supermarket_system.dto.cart.UpdateCartItemQuantityRequestDto;
import com.supermarket.supermarket_system.mappers.CartMapper;
import com.supermarket.supermarket_system.models.Cart;
import com.supermarket.supermarket_system.services.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public CartResponseDto getCart(@RequestHeader("X-User-Id") Long userId) {
        Cart cart = cartService.getCartByUserId(userId);
        return CartMapper.toDto(cart);
    }



    @PutMapping("/items/{cartItemId}")
    public CartResponseDto updateItemQuantity(@RequestHeader("X-User-Id") Long userId,
                                              @PathVariable Long cartItemId,
                                              @Valid @RequestBody UpdateCartItemQuantityRequestDto request) {
        Cart cart = cartService.updateItemQuantity(userId, cartItemId, request.getQuantity());
        return CartMapper.toDto(cart);
    }

    @DeleteMapping("/items/{cartItemId}")
    public CartResponseDto removeItem(@RequestHeader("X-User-Id") Long userId,
                                      @PathVariable Long cartItemId) {
        Cart cart = cartService.removeItem(userId, cartItemId);
        return CartMapper.toDto(cart);
    }

    @DeleteMapping
    public CartResponseDto clearCart(@RequestHeader("X-User-Id") Long userId) {
        Cart cart = cartService.clearCart(userId);
        return CartMapper.toDto(cart);
    }
}
