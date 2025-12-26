package com.supermarket.supermarket_system.service;

import com.supermarket.supermarket_system.dto.cart.CartCheckoutEvent;
import com.supermarket.supermarket_system.dto.cart.ItemDetailsDto;
import com.supermarket.supermarket_system.model.Order;
import com.supermarket.supermarket_system.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    // Eureka service names (LoadBalanced RestTemplate) to call Cart and Items
    private String itemServiceUrl() {
        return "http://Items/items";
    }

    private String cartServiceUrl() {
        return "http://Cart/cart";
    }

    // 1. Create Order from Cart
    public Order createOrderFromCheckoutEvent(CartCheckoutEvent event) {
        log.info("========== CREATING ORDER FROM CHECKOUT EVENT ==========");
        log.info("User ID: {}", event.getUserId());
        log.info("Payment Method: {}", event.getPaymentMethod());
        log.info("Total Price: {}", event.getTotalPrice());
        log.info("ItemDetails count: {}", event.getItemDetails() != null ? event.getItemDetails().size() : "NULL");

        // Debug: Log itemDetails content
        if (event.getItemDetails() != null && !event.getItemDetails().isEmpty()) {
            event.getItemDetails().forEach((itemId, details) -> {
                log.info("  Item {}: name={}, qty={}, price={}, subtotal={}",
                        itemId, details.getName(), details.getQuantity(),
                        details.getUnitPrice(), details.getSubtotal());
            });
        } else {
            log.warn("⚠️ WARNING: ItemDetails is NULL or EMPTY in checkout event!");
        }

        // Create new order
        Order order = new Order();
        order.setUserId(event.getUserId());
        order.setItemDetails(event.getItemDetails());
        order.setPaymentMethod(event.getPaymentMethod());
        order.setTotalAmount(event.getTotalPrice());
        order.setStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());

        // Save order
        Order savedOrder = orderRepository.save(order);

        log.info("✅ Order {} created successfully", savedOrder.getId());
        log.info("   - User: {}", savedOrder.getUserId());
        log.info("   - Total: {}", savedOrder.getTotalAmount());
        log.info("   - ItemDetails saved: {}",
                savedOrder.getItemDetails() != null ? savedOrder.getItemDetails().size() : "NULL");
        log.info("========================================================");

        return savedOrder;
    }

    // 2. Get Order by ID
    public Order getOrderById(Long id) {
        log.debug("Fetching order with ID: {}", id);
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    // 3. Get User Orders (sorted by date, newest first)
    public List<Order> getUserOrders(Long userId) {
        log.debug("Fetching all orders for user: {}", userId);
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    // 4. Get All Orders (sorted by date, newest first)
    public List<Order> getAllOrders() {
        log.debug("Fetching all orders");
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    // 5. Get Orders by Status (sorted by date)
    public List<Order> getOrdersByStatus(String status) {
        log.debug("Fetching orders with status: {}", status);
        return orderRepository.findByStatusOrderByOrderDateDesc(status);
    }

    // 6. Get User Orders by Status (sorted by date)
    public List<Order> getUserOrdersByStatus(Long userId, String status) {
        log.debug("Fetching orders for user: {} with status: {}", userId, status);
        return orderRepository.findByUserIdAndStatusOrderByOrderDateDesc(userId, status);
    }

    // 7. Update Order Status
    public Order updateOrderStatus(Long id, String status) {
        log.info("Updating order {} status to: {}", id, status);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        String oldStatus = order.getStatus();

        // Validate status transition
        validateStatusTransition(oldStatus, status);

        order.setStatus(status);

        // If order is cancelled, restore item quantities
        if ("CANCELLED".equalsIgnoreCase(status) && !"CANCELLED".equalsIgnoreCase(oldStatus)) {
            updateItemQuantities(order.getItemDetails(), true);
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated successfully from {} to {}", oldStatus, status);
        return updatedOrder;
    }

    // 8. Cancel Order
    public Order cancelOrder(Long orderId) {
        log.info("Cancelling order: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        String currentStatus = order.getStatus();

        // Prevent cancelling delivered or already cancelled orders
        if ("DELIVERED".equalsIgnoreCase(currentStatus)) {
            throw new RuntimeException("Cannot cancel order that has already been delivered");
        }

        if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
            throw new RuntimeException("Order is already cancelled");
        }

        // Restore item quantities back to inventory
        updateItemQuantities(order.getItemDetails(), true);

        // Set status to CANCELLED
        order.setStatus("CANCELLED");
        Order cancelledOrder = orderRepository.save(order);

        log.info("Order {} cancelled successfully, items restored to inventory", orderId);
        return cancelledOrder;
    }

    // 9. Update Item Quantities (Deduct or Restore)
    private void updateItemQuantities(Map<String, ItemDetailsDto> itemDetails, boolean restore) {
        itemDetails.forEach((itemIdStr, details) -> {
            try {
                Long itemId = Long.parseLong(itemIdStr);
                int quantity = details.getQuantity();

                if (restore) {
                    // Restore quantities by calling the /items/restore endpoint
                    Map<String, Object> restoreRequest = new HashMap<>();
                    restoreRequest.put("itemId", itemId);
                    restoreRequest.put("quantity", quantity);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(restoreRequest, headers);

                    ResponseEntity<Map> response = restTemplate.postForEntity(
                            itemServiceUrl() + "/restore",
                            entity,
                            Map.class
                    );

                    if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                        throw new RuntimeException("Items service did not respond properly for restoration");
                    }

                    Map<String, Object> respMap = response.getBody();
                    boolean success = Boolean.TRUE.equals(respMap.get("success"));

                    if (!success) {
                        String message = (String) respMap.getOrDefault("message", "Failed to restore item quantity");
                        throw new RuntimeException(message);
                    }

                    log.debug("Restored quantity for item {}: +{}", itemId, quantity);
                } else {
                    // Deduct quantities by calling the /items/deduct endpoint
                    Map<String, Object> deductRequest = new HashMap<>();
                    deductRequest.put("itemId", itemId);
                    deductRequest.put("quantity", quantity);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(deductRequest, headers);

                    ResponseEntity<Map> response = restTemplate.postForEntity(
                            itemServiceUrl() + "/deduct",
                            entity,
                            Map.class
                    );

                    if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                        throw new RuntimeException("Items service did not respond properly for deduction");
                    }

                    Map<String, Object> respMap = response.getBody();
                    boolean success = Boolean.TRUE.equals(respMap.get("success"));

                    if (!success) {
                        String message = (String) respMap.getOrDefault("message", "Failed to deduct item quantity");
                        throw new RuntimeException(message);
                    }

                    log.debug("Deducted quantity for item {}: -{}", itemId, quantity);
                }
            } catch (Exception e) {
                log.error("Failed to update quantity for item ID: {}, restore: {}", itemIdStr, restore, e);
                throw new RuntimeException("Failed to update item quantity for item: " + itemIdStr + ". " + e.getMessage());
            }
        });
    }

    // 10. Validate Status Transition (Helper)
    private void validateStatusTransition(String currentStatus, String newStatus) {
        log.debug("Validating status transition from {} to {}", currentStatus, newStatus);

        // Cannot change delivered orders
        if ("DELIVERED".equalsIgnoreCase(currentStatus)) {
            throw new RuntimeException("Cannot change status of delivered order");
        }

        // Cannot change cancelled orders
        if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
            throw new RuntimeException("Cannot change status of cancelled order");
        }

        // Valid status progression: PENDING -> PROCESSING -> SHIPPING -> DELIVERED
        // Can cancel from PENDING, PROCESSING, or SHIPPING
        if ("PENDING".equalsIgnoreCase(currentStatus)) {
            if (!("PROCESSING".equalsIgnoreCase(newStatus) || "CANCELLED".equalsIgnoreCase(newStatus))) {
                throw new RuntimeException("Invalid status transition from PENDING to " + newStatus);
            }
        } else if ("PROCESSING".equalsIgnoreCase(currentStatus)) {
            if (!("SHIPPING".equalsIgnoreCase(newStatus) || "CANCELLED".equalsIgnoreCase(newStatus))) {
                throw new RuntimeException("Invalid status transition from PROCESSING to " + newStatus);
            }
        } else if ("SHIPPING".equalsIgnoreCase(currentStatus)) {
            if (!("DELIVERED".equalsIgnoreCase(newStatus) || "CANCELLED".equalsIgnoreCase(newStatus))) {
                throw new RuntimeException("Invalid status transition from SHIPPING to " + newStatus);
            }
        }

        log.debug("Status transition validated successfully");
    }


}