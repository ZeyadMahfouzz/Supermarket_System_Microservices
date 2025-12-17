package com.supermarket.supermarket_system.service;

import com.supermarket.supermarket_system.model.Order;
import com.supermarket.supermarket_system.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
        Map<Long, Integer> items = (Map<Long, Integer>) cartItems.get("items");
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
        enrichOrderWithItemDetails(order);
        return order;
    }

    // 3. Get User Orders (sorted by date, newest first)
    public List<Order> getUserOrders(Long userId) {
        log.debug("Fetching all orders for user: {}", userId);
        List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId);
        orders.forEach(this::enrichOrderWithItemDetails);
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
        orders.forEach(this::enrichOrderWithItemDetails);
        return orders;
    }

    // 5. Get Orders by Status (sorted by date)
    public List<Order> getOrdersByStatus(String status) {
        log.debug("Fetching orders with status: {}", status);
        List<Order> orders = orderRepository.findByStatusOrderByOrderDateDesc(status);
        orders.forEach(this::enrichOrderWithItemDetails);
        return orders;
    }

    // 6. Get User Orders by Status (sorted by date)
    public List<Order> getUserOrdersByStatus(Long userId, String status) {
        log.debug("Fetching orders for user: {} with status: {}", userId, status);
        List<Order> orders = orderRepository.findByUserIdAndStatusOrderByOrderDateDesc(userId, status);
        orders.forEach(this::enrichOrderWithItemDetails);
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

    // 9. Enrich Order with Item Details (Helper)
    private void enrichOrderWithItemDetails(Order order) {
        Map<String, Object> details = new HashMap<>();
        order.getItems().forEach((itemId, quantity) -> {
            try {
                Map itemData = restTemplate.getForObject(itemServiceUrl() + "/" + itemId, Map.class);
                if (itemData != null) {
                    Map<String, Object> itemInfo = new HashMap<>();
                    itemInfo.put("quantity", quantity);
                    itemInfo.put("price", itemData.get("price"));
                    itemInfo.put("subtotal", ((Number) itemData.get("price")).doubleValue() * quantity);
                    details.put((String) itemData.get("name"), itemInfo);
                }
            } catch (Exception e) {
                log.error("Failed to fetch item details for item ID: {}", itemId, e);
            }
        });
        order.setItemDetails(details);
    }

    private void updateItemQuantities(Map<Long, Integer> items, boolean restore) {
        items.forEach((itemId, quantity) -> {
            try {
                int adjustment = restore ? quantity : -quantity;
                restTemplate.put(itemServiceUrl() + "/" + itemId + "/quantity?adjustment=" + adjustment, null);
                log.debug("Updated quantity for item {}: adjustment {}", itemId, adjustment);
            } catch (Exception e) {
                log.error("Failed to update quantity for item ID: {}, adjustment: {}", itemId, restore ? quantity : -quantity, e);
                throw new RuntimeException("Failed to update item quantity for item: " + itemId);
            }
        });
    }

    // Helper: Validate stock availability and update quantities
    private void validateAndUpdateStock(Map<Long, Integer> items, boolean restore) {
        items.forEach((itemId, quantity) -> {
            try {
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
                log.error("Failed to validate/update stock for item ID: {}", itemId, e);
                throw new RuntimeException("Failed to process item: " + itemId + ". " + e.getMessage());
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
