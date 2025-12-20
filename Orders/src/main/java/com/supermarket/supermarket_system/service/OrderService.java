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

    // Use Eureka service names (LoadBalanced RestTemplate) to call Cart and Items
    private String itemServiceUrl() {
        return "http://Items/items";
    }

    private String cartServiceUrl() {
        return "http://Cart/cart";
    }

    // 1. Create Order from Cart
    public Order createOrderFromCart(Long userId, String paymentMethod) {
        log.info("Creating order from cart for user: {}", userId);

        // Fetch cart items
        Map cartItems;
        try {
            cartItems = restTemplate.getForObject(cartServiceUrl() + "/" + userId, Map.class);
            if (cartItems == null || ((Map) cartItems.get("items")).isEmpty()) {
                throw new RuntimeException("Cart is empty for user: " + userId);
            }
        } catch (Exception e) {
            log.error("Failed to fetch cart for user: {}", userId, e);
            throw new RuntimeException("Could not retrieve cart for user: " + userId);
        }

        // Validate stock availability and decrease quantities
        Map<String, Integer> items = (Map<String, Integer>) cartItems.get("items");
        validateAndUpdateStock(items, false);

        // Create order
        Order order = new Order();
        order.setUserId(userId);
        order.setItems(items);
        order.setPaymentMethod(paymentMethod);
        order.setStatus("PENDING");

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());

        // Clear cart after successful order
        try {
            restTemplate.delete(cartServiceUrl() + "/" + userId);
            log.info("Cart cleared for user: {}", userId);
        } catch (Exception e) {
            log.warn("Failed to clear cart for user: {}, but order was created", userId, e);
        }

        return savedOrder;
    }

    public Order createOrder(Order order) {
        log.info("Creating order for user: {}", order.getUserId());
        // Validate stock and update item quantities via REST call to Item Service
        validateAndUpdateStock(order.getItems(), false);
        order.setStatus("PENDING");
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return savedOrder;
    }

    // 2. Get Order by ID
    public Order getOrderById(Long id) {
        log.debug("Fetching order with ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        // Only enrich if itemDetails is empty (for old orders)
        if (order.getItemDetails() == null || order.getItemDetails().isEmpty()) {
            enrichOrderWithItemDetails(order);
        }

        return order;
    }

    // 3. Get User Orders (sorted by date, newest first)
    public List<Order> getUserOrders(Long userId) {
        log.debug("Fetching all orders for user: {}", userId);
        List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId);

        // Only enrich orders that don't have itemDetails
        orders.forEach(order -> {
            if (order.getItemDetails() == null || order.getItemDetails().isEmpty()) {
                enrichOrderWithItemDetails(order);
            }
        });

        return orders;
    }

    // Legacy method - keeping for backward compatibility
    public List<Order> getOrdersByUserId(Long userId) {
        return getUserOrders(userId);
    }

    // 4. Get All Orders (sorted by date, newest first)
    public List<Order> getAllOrders() {
        log.debug("Fetching all orders");
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();

        // Only enrich orders that don't have itemDetails
        orders.forEach(order -> {
            if (order.getItemDetails() == null || order.getItemDetails().isEmpty()) {
                enrichOrderWithItemDetails(order);
            }
        });

        return orders;
    }

    // 5. Get Orders by Status (sorted by date)
    public List<Order> getOrdersByStatus(String status) {
        log.debug("Fetching orders with status: {}", status);
        List<Order> orders = orderRepository.findByStatusOrderByOrderDateDesc(status);

        // Only enrich orders that don't have itemDetails
        orders.forEach(order -> {
            if (order.getItemDetails() == null || order.getItemDetails().isEmpty()) {
                enrichOrderWithItemDetails(order);
            }
        });

        return orders;
    }

    // 6. Get User Orders by Status (sorted by date)
    public List<Order> getUserOrdersByStatus(Long userId, String status) {
        log.debug("Fetching orders for user: {} with status: {}", userId, status);
        List<Order> orders = orderRepository.findByUserIdAndStatusOrderByOrderDateDesc(userId, status);

        // Only enrich orders that don't have itemDetails
        orders.forEach(order -> {
            if (order.getItemDetails() == null || order.getItemDetails().isEmpty()) {
                enrichOrderWithItemDetails(order);
            }
        });

        return orders;
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
            updateItemQuantities(order.getItems(), true);
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
        updateItemQuantities(order.getItems(), true);

        // Set status to CANCELLED
        order.setStatus("CANCELLED");
        Order cancelledOrder = orderRepository.save(order);

        log.info("Order {} cancelled successfully, items restored to inventory", orderId);
        return cancelledOrder;
    }

    // 9. Enrich Order with Item Details (Helper) - For old orders without itemDetails
    private void enrichOrderWithItemDetails(Order order) {
        Map<String, ItemDetailsDto> details = new HashMap<>();

        order.getItems().forEach((itemId, quantity) -> {
            try {
                Map itemData = restTemplate.getForObject(itemServiceUrl() + "/" + itemId, Map.class);
                if (itemData != null) {
                    ItemDetailsDto dto = new ItemDetailsDto();
                    dto.setName((String) itemData.get("name"));
                    dto.setImageUrl((String) itemData.get("imageUrl"));
                    dto.setQuantity(quantity);

                    Double price = itemData.get("price") != null
                            ? ((Number) itemData.get("price")).doubleValue()
                            : 0.0;
                    dto.setUnitPrice(price);
                    dto.setSubtotal(price * quantity);

                    details.put(itemId, dto);
                }
            } catch (Exception e) {
                log.error("Failed to fetch item details for item ID: {}", itemId, e);
            }
        });

        order.setItemDetails(details);
    }

    private void updateItemQuantities(Map<String, Integer> items, boolean restore) {
        items.forEach((itemIdStr, quantity) -> {
            try {
                Long itemId = Long.parseLong(itemIdStr);

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

    // Helper: Validate stock availability and update quantities
    private void validateAndUpdateStock(Map<String, Integer> items, boolean restore) {
        items.forEach((itemIdStr, quantity) -> {
            try {
                Long itemId = Long.parseLong(itemIdStr);

                // Check stock availability before ordering
                if (!restore) {
                    Map itemData = restTemplate.getForObject(itemServiceUrl() + "/" + itemId, Map.class);
                    if (itemData == null) {
                        throw new RuntimeException("Item not found: " + itemId);
                    }
                    Integer availableQuantity = (Integer) itemData.get("quantity");
                    if (availableQuantity == null || availableQuantity < quantity) {
                        throw new RuntimeException("Insufficient stock for item: " + itemId +
                                ". Available: " + availableQuantity + ", Requested: " + quantity);
                    }
                }

                // Update quantities
                int adjustment = restore ? quantity : -quantity;
                restTemplate.put(itemServiceUrl() + "/" + itemId + "/quantity?adjustment=" + adjustment, null);
                log.debug("Stock validated and updated for item {}: adjustment {}", itemId, adjustment);
            } catch (Exception e) {
                log.error("Failed to validate/update stock for item ID: {}", itemIdStr, e);
                throw new RuntimeException("Failed to process item: " + itemIdStr + ". " + e.getMessage());
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

    // ✅ FIXED: Create order from checkout event with itemDetails
    public Order createOrderFromCheckoutEvent(CartCheckoutEvent event) {
        log.info("========== CREATING ORDER FROM CHECKOUT EVENT ==========");
        log.info("User ID: {}", event.getUserId());
        log.info("Payment Method: {}", event.getPaymentMethod());
        log.info("Total Price: {}", event.getTotalPrice());
        log.info("Items count: {}", event.getItems() != null ? event.getItems().size() : "NULL");
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
        order.setItems(event.getItems()); // Already Map<String, Integer>
        order.setItemDetails(event.getItemDetails()); // ✅ THIS IS THE FIX
        order.setPaymentMethod(event.getPaymentMethod());
        order.setTotalAmount(event.getTotalPrice()); // ✅ Set total amount
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
}