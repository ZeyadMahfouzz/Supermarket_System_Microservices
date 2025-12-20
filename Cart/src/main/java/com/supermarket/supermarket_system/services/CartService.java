package com.supermarket.supermarket_system.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermarket.supermarket_system.dto.cart.AddCartItemRequestDto;
import com.supermarket.supermarket_system.dto.cart.CartCheckoutEvent;
import com.supermarket.supermarket_system.models.Cart;
import com.supermarket.supermarket_system.models.CartItem;
import com.supermarket.supermarket_system.repositories.CartRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

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

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.rabbitmq.item-exchange:items.exchange}")
    private String itemExchange;

    @Value("${app.rabbitmq.item-routing-key:items.routingkey}")
    private String itemRoutingKey;

    @Value("${app.items.service.url:http://items}")
    private String itemsServiceUrl;

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
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Items service did not respond for deduction"
            );
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

        // Get name and imageUrl from Items service response
        String itemName = (String) respMap.get("name");
        String imageUrl = (String) respMap.get("imageUrl");

        // Debug logging
        System.out.println("=== DEBUG: Items Service Response ===");
        System.out.println("Item ID: " + request.getItemId());
        System.out.println("Unit Price: " + unitPrice);
        System.out.println("Item Name: " + itemName);
        System.out.println("Image URL: " + imageUrl);
        System.out.println("Full Response Map: " + respMap);
        System.out.println("===================================");

        if (!available) {
            throw new IllegalArgumentException("Item not available");
        }

        if (availableQuantity < request.getQuantity()) {
            throw new IllegalArgumentException("Not enough quantity available");
        }

        return this.addItemWithDetails(
                userId,
                request.getItemId(),
                request.getQuantity(),
                unitPrice,
                itemName,
                imageUrl
        );
    }

    public Cart addItemWithDetails(Long userId, Long itemId, int quantity, Double unitPrice, String name, String imageUrl) {
        Cart cart = getCartByUserId(userId);

        CartItem existing = cart.getItems().stream()
                .filter(ci -> ci.getItemId().equals(itemId))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            if (unitPrice != null) existing.setUnitPrice(unitPrice);
            if (name != null) existing.setName(name);
            if (imageUrl != null) existing.setImageUrl(imageUrl);
        } else {
            cart.addItem(new CartItem(itemId, quantity, unitPrice, name, imageUrl));
        }

        return cartRepository.save(cart);
    }

    public void checkout(Long userId, String paymentMethod) {
        Cart cart = getCartByUserId(userId);

        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // Step 1: Deduct quantities from Items service via HTTP for each cart item
        for (CartItem item : cart.getItems()) {
            Map<String, Object> deductRequest = new HashMap<>();
            deductRequest.put("itemId", item.getItemId());
            deductRequest.put("quantity", item.getQuantity());

            try {
                // HTTP POST call to Items service
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(deductRequest, headers);

                ResponseEntity<Map> response = restTemplate.postForEntity(
                        itemsServiceUrl + "/items/deduct",
                        entity,
                        Map.class
                );

                if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                    throw new ResponseStatusException(
                            HttpStatus.SERVICE_UNAVAILABLE,
                            "Items service did not respond properly for deduction"
                    );
                }

                Map<String, Object> respMap = response.getBody();
                boolean success = Boolean.TRUE.equals(respMap.get("success"));

                if (!success) {
                    String message = (String) respMap.getOrDefault("message", "Failed to deduct item quantity");
                    throw new IllegalStateException(message);
                }
            } catch (Exception e) {
                if (e instanceof IllegalStateException) {
                    throw e;
                }
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Failed to communicate with Items service: " + e.getMessage()
                );
            }
        }

        // Step 2: Calculate total price
        double totalPrice = cart.getItems().stream()
                .mapToDouble(ci -> (ci.getUnitPrice() == null ? 0.0 : ci.getUnitPrice()) * ci.getQuantity())
                .sum();

        // Step 3: Build checkout event with ALL data needed by Orders service
        CartCheckoutEvent event = new CartCheckoutEvent();
        event.setUserId(userId);
        event.setPaymentMethod(paymentMethod);
        event.setTotalPrice(totalPrice);

        // Include complete item data with prices
        Map<String, Integer> itemsMap = new HashMap<>();
        Map<String, Double> itemPricesMap = new HashMap<>();

        for (CartItem ci : cart.getItems()) {
            String itemIdStr = String.valueOf(ci.getItemId());
            itemsMap.put(itemIdStr, ci.getQuantity());
            itemPricesMap.put(itemIdStr, ci.getUnitPrice());
        }

        event.setItems(itemsMap);
        event.setItemPrices(itemPricesMap);

        // Step 4: Publish event to Orders service via RabbitMQ
        cartPublisher.publishCheckout(event);

        // Step 5: Clear the cart after successful checkout
        clearCart(userId);
    }
}