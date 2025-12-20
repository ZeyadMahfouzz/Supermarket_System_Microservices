package com.supermarket.supermarket_system.controllers;

import com.supermarket.supermarket_system.dto.cart.CartResponseDto;
import com.supermarket.supermarket_system.dto.cart.UpdateCartItemQuantityRequestDto;
import com.supermarket.supermarket_system.dto.cart.AddCartItemRequestDto;
import com.supermarket.supermarket_system.mappers.CartMapper;
import com.supermarket.supermarket_system.models.Cart;
import com.supermarket.supermarket_system.services.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody String paymentMethod) {

        try {
            cartService.checkout(userId, paymentMethod);
            return ResponseEntity.ok("Checkout completed successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Checkout failed: " + e.getMessage());
        }
    }

    @PostMapping("/items")
    public ResponseEntity<?> addItemToCart(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddCartItemRequestDto request) {

        Cart cart = cartService.addItemToCart(userId, request);
        return ResponseEntity.ok(CartMapper.toDto(cart));
    }

    @PutMapping("/items")
    public CartResponseDto updateItemQuantity(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateCartItemQuantityRequestDto request) {

        Cart cart = cartService.updateItemQuantity(userId, request.getCartItemId(), request.getQuantity());
        return CartMapper.toDto(cart);
    }

    @DeleteMapping("/items")
    public CartResponseDto removeItem(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateCartItemQuantityRequestDto request) {

        Cart cart = cartService.removeItem(userId, request.getCartItemId());
        return CartMapper.toDto(cart);
    }

    @DeleteMapping
    public CartResponseDto clearCart(@RequestHeader("X-User-Id") Long userId) {
        Cart cart = cartService.clearCart(userId);
        return CartMapper.toDto(cart);
    }
}