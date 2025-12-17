package com.supermarket.supermarket_system.services;

import com.supermarket.supermarket_system.models.Cart;
import com.supermarket.supermarket_system.models.CartItem;
import com.supermarket.supermarket_system.repositories.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;

    @Autowired
    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(userId)));
    }

    public Cart addItem(Long userId, Long itemId, int quantity, Double unitPrice) {
        Cart cart = getCartByUserId(userId);

        CartItem existing = cart.getItems().stream()
                .filter(ci -> ci.getItemId().equals(itemId))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            if (unitPrice != null) existing.setUnitPrice(unitPrice);
        } else {
            cart.addItem(new CartItem(itemId, quantity, unitPrice));
        }

        return cartRepository.save(cart);
    }

    public Cart updateItemQuantity(Long userId, Long cartItemId, int quantity) {
        Cart cart = getCartByUserId(userId);

        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (quantity <= 0) {
            cart.removeItem(item);
        } else {
            item.setQuantity(quantity);
        }

        return cartRepository.save(cart);
    }

    public Cart removeItem(Long userId, Long cartItemId) {
        Cart cart = getCartByUserId(userId);

        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        cart.removeItem(item);
        return cartRepository.save(cart);
    }

    public Cart clearCart(Long userId) {
        Cart cart = getCartByUserId(userId);
        cart.getItems().clear();
        return cartRepository.save(cart);
    }
}
