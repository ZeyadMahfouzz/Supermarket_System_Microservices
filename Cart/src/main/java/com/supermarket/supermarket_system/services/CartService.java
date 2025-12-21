package com.supermarket.supermarket_system.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermarket.supermarket_system.dto.cart.AddCartItemRequestDto;
import com.supermarket.supermarket_system.dto.payment.CartCheckoutEvent;
import com.supermarket.supermarket_system.dto.payment.*;
import com.supermarket.supermarket_system.models.Cart;
import com.supermarket.supermarket_system.models.CartItem;
import com.supermarket.supermarket_system.models.PaymentMethod;
import com.supermarket.supermarket_system.repositories.CartRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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

    @Value("${app.payment.service.url:lb://Payment}")
    private String paymentServiceUrl;

    @Autowired
    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(userId)));
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

    // NEW checkout method with payment details
    @Transactional
    public CheckoutResponseDto checkout(Long userId, CheckoutRequestDto request) {
        // 1. Get user's cart
        Cart cart = getCartByUserId(userId);

        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // 2. Validate payment method specific data
        validatePaymentMethodData(request);

        // 3. Validate expiry date for card payments
        if (request.getPaymentMethod() == PaymentMethod.CREDIT_CARD ||
                request.getPaymentMethod() == PaymentMethod.DEBIT_CARD) {
            validateExpiryDate(request);
        }

        // 4. Deduct quantities from Items service via HTTP for each cart item
        for (CartItem item : cart.getItems()) {
            Map<String, Object> deductRequest = new HashMap<>();
            deductRequest.put("itemId", item.getItemId());
            deductRequest.put("quantity", item.getQuantity());

            try {
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

        // 5. Calculate total price
        double totalPrice = cart.getItems().stream()
                .mapToDouble(ci -> (ci.getUnitPrice() == null ? 0.0 : ci.getUnitPrice()) * ci.getQuantity())
                .sum();

        // 6. Build checkout event for Orders service
        CartCheckoutEvent event = new CartCheckoutEvent();
        event.setUserId(userId);
        event.setPaymentMethod(request.getPaymentMethod().name());
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

        Map<String, ItemDetailsDto> itemDetails = new HashMap<>();

        for (CartItem ci : cart.getItems()) {
            ItemDetailsDto dto = new ItemDetailsDto();
            dto.setName(ci.getName());
            dto.setImageUrl(ci.getImageUrl());
            dto.setUnitPrice(ci.getUnitPrice());
            dto.setQuantity(ci.getQuantity());
            dto.setSubtotal(ci.getUnitPrice() * ci.getQuantity());

            itemDetails.put(String.valueOf(ci.getItemId()), dto);
        }

        event.setItemDetails(itemDetails);


        // 7. Publish event to Orders service via RabbitMQ
        cartPublisher.publishCheckout(event);

        // Note: In a real scenario, you'd wait for order creation confirmation
        // For now, we'll simulate an orderId based on timestamp
        Long orderId = System.currentTimeMillis();

        // 8. Call Payment Service to process payment
        PaymentRequestDto paymentRequest = buildPaymentRequest(userId, orderId, totalPrice, request);
        PaymentResponseDto paymentResponse = callPaymentService(paymentRequest);

        // 9. Clear cart after successful payment
        clearCart(userId);

        // 10. Return response
        return new CheckoutResponseDto(
                "Checkout completed successfully",
                orderId,
                paymentResponse.getPaymentId(),
                paymentResponse.getTransactionId(),
                paymentResponse.getStatus(),
                totalPrice
        );
    }

    private PaymentRequestDto buildPaymentRequest(Long userId, Long orderId, Double amount, CheckoutRequestDto request) {
        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setUserId(userId);
        paymentRequest.setOrderId(orderId);
        paymentRequest.setAmount(amount);
        paymentRequest.setPaymentMethod(request.getPaymentMethod());

        // Set payment method specific details
        switch (request.getPaymentMethod()) {
            case CREDIT_CARD:
                paymentRequest.setCreditCardPayment(request.getCreditCardPayment());
                break;
            case DEBIT_CARD:
                paymentRequest.setDebitCardPayment(request.getDebitCardPayment());
                break;
            case MOBILE_PAYMENT:
                paymentRequest.setMobilePayment(request.getMobilePayment());
                break;
            case BANK_TRANSFER:
                paymentRequest.setBankTransfer(request.getBankTransfer());
                break;
            case CASH:
                paymentRequest.setCashPayment(request.getCashPayment());
                break;
        }

        return paymentRequest;
    }

    private PaymentResponseDto callPaymentService(PaymentRequestDto paymentRequest) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", String.valueOf(paymentRequest.getUserId()));

            HttpEntity<PaymentRequestDto> entity = new HttpEntity<>(paymentRequest, headers);

            ResponseEntity<PaymentResponseDto> response = restTemplate.postForEntity(
                    paymentServiceUrl + "/payment/process",
                    entity,
                    PaymentResponseDto.class
            );

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                return response.getBody();
            } else {
                throw new RuntimeException("Payment service returned error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process payment: " + e.getMessage(), e);
        }
    }

    private void validatePaymentMethodData(CheckoutRequestDto request) {
        switch (request.getPaymentMethod()) {
            case CREDIT_CARD:
                if (request.getCreditCardPayment() == null) {
                    throw new IllegalArgumentException("Credit card payment details are required");
                }
                break;
            case DEBIT_CARD:
                if (request.getDebitCardPayment() == null) {
                    throw new IllegalArgumentException("Debit card payment details are required");
                }
                break;
            case MOBILE_PAYMENT:
                if (request.getMobilePayment() == null) {
                    throw new IllegalArgumentException("Mobile payment details are required");
                }
                break;
            case BANK_TRANSFER:
                if (request.getBankTransfer() == null) {
                    throw new IllegalArgumentException("Bank transfer details are required");
                }
                break;
            case CASH:
                // Auto-create and confirm cash payment if missing
                if (request.getCashPayment() == null) {
                    request.setCashPayment(new CashPaymentDto());
                }
                if (request.getCashPayment().getConfirmed() == null || !request.getCashPayment().getConfirmed()) {
                    request.getCashPayment().setConfirmed(true);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid payment method");
        }
    }

    private void validateExpiryDate(CheckoutRequestDto request) {
        String expiryDate = null;

        if (request.getPaymentMethod() == PaymentMethod.CREDIT_CARD) {
            expiryDate = request.getCreditCardPayment().getExpiryDate();
        } else if (request.getPaymentMethod() == PaymentMethod.DEBIT_CARD) {
            expiryDate = request.getDebitCardPayment().getExpiryDate();
        }

        if (expiryDate == null) {
            throw new IllegalArgumentException("Expiry date is required");
        }

        try {
            // Parse MM/YY format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth cardExpiry = YearMonth.parse(expiryDate, formatter);
            YearMonth currentMonth = YearMonth.now();

            if (cardExpiry.isBefore(currentMonth)) {
                throw new IllegalArgumentException("Card has expired");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid expiry date format or expired card");
        }
    }
}