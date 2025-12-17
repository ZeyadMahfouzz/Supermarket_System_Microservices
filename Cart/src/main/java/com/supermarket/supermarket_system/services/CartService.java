package com.supermarket.supermarket_system.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermarket.supermarket_system.dto.cart.AddCartItemRequestDto;
import com.supermarket.supermarket_system.dto.cart.CartCheckoutEvent;
import com.supermarket.supermarket_system.models.Cart;
import com.supermarket.supermarket_system.models.CartItem;
import com.supermarket.supermarket_system.repositories.CartRepository;
import jakarta.ws.rs.ServiceUnavailableException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartPublisher cartPublisher;

    @Value("${app.rabbitmq.item-exchange:items.exchange}")
    private String itemExchange;

    @Value("${app.rabbitmq.item-routing-key:items.routingkey}")
    private String itemRoutingKey;

    @Value("${app.rabbitmq.item-deduct-routing-key:items.deduct.routingkey}")
    private String itemDeductRoutingKey;

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

    public Cart addItemToCart(Long userId, AddCartItemRequestDto request) {

        // Build RPC request
        Map<String, Object> req = new HashMap<>();
        req.put("itemId", request.getItemId());
        req.put("requestedQuantity", request.getQuantity());

        // RPC call
        Object respObj = rabbitTemplate.convertSendAndReceive(
                itemExchange,
                itemRoutingKey,
                req
        );

        if (respObj == null) {
            throw new ServiceUnavailableException("Items service did not respond");
        }

        Map<String, Object> respMap;
        if (respObj instanceof Map<?, ?> map) {
            respMap = (Map<String, Object>) map;
        } else {
            try {
                respMap = objectMapper.convertValue(respObj, Map.class);
            } catch (Exception e) {
                throw new IllegalStateException("Invalid response from Items service");
            }
        }

        boolean available = Boolean.TRUE.equals(respMap.get("available"));
        int availableQuantity =
                ((Number) respMap.getOrDefault("availableQuantity", 0)).intValue();

        // Get price from Items service response (not from request)
        if (respMap.get("unitPrice") == null) {
            throw new IllegalStateException("Items service did not return unit price");
        }
        double unitPrice = ((Number) respMap.get("unitPrice")).doubleValue();

        if (!available) {
            throw new IllegalArgumentException("Item not available");
        }

        if (availableQuantity < request.getQuantity()) {
            throw new IllegalArgumentException("Not enough quantity available");
        }

        return this.addItem(
                userId,
                request.getItemId(),
                request.getQuantity(),
                unitPrice
        );
    }

    public void checkout(Long userId, String paymentMethod) {
        Cart cart = getCartByUserId(userId);

        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // Step 1: Deduct quantities from Items service for each cart item
        for (CartItem item : cart.getItems()) {
            Map<String, Object> deductRequest = new HashMap<>();
            deductRequest.put("itemId", item.getItemId());
            deductRequest.put("quantity", item.getQuantity());

            // RPC call to deduct quantity
            Object respObj = rabbitTemplate.convertSendAndReceive(
                    itemExchange,
                    itemDeductRoutingKey,
                    deductRequest
            );

            if (respObj == null) {
                throw new ServiceUnavailableException("Items service did not respond for deduction");
            }

            Map<String, Object> respMap;
            if (respObj instanceof Map<?, ?> map) {
                respMap = (Map<String, Object>) map;
            } else {
                try {
                    respMap = objectMapper.convertValue(respObj, Map.class);
                } catch (Exception e) {
                    throw new IllegalStateException("Invalid response from Items service");
                }
            }

            boolean success = Boolean.TRUE.equals(respMap.get("success"));
            if (!success) {
                String message = (String) respMap.getOrDefault("message", "Failed to deduct item quantity");
                throw new IllegalStateException(message);
            }
        }

        // Step 2: Calculate total price
        double totalPrice = cart.getItems().stream()
                .mapToDouble(ci -> (ci.getUnitPrice() == null ? 0.0 : ci.getUnitPrice()) * ci.getQuantity())
                .sum();

        // Step 3: Build and publish checkout event
        CartCheckoutEvent event = new CartCheckoutEvent();
        event.setUserId(userId);

        // Convert itemId to String keys for safe JSON map keys
        Map<String, Integer> itemsMap = new HashMap<>();
        for (CartItem ci : cart.getItems()) {
            itemsMap.put(String.valueOf(ci.getItemId()), ci.getQuantity());
        }

        event.setItems(itemsMap);
        event.setPaymentMethod(paymentMethod);
        event.setTotalPrice(totalPrice);

        // Publish event to Orders service
        cartPublisher.publishCheckout(event);

        // Step 4: Clear the cart after successful checkout
        clearCart(userId);
    }
}