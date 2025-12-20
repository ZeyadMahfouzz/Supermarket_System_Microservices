package com.supermarket.supermarket_system.controller;

import com.supermarket.supermarket_system.dto.OrderMapper;
import com.supermarket.supermarket_system.dto.OrderResponseDto;
import com.supermarket.supermarket_system.dto.UpdateStatusRequestDto;
import com.supermarket.supermarket_system.model.Order;
import com.supermarket.supermarket_system.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    // Get specific order by ID (OWNER or ADMIN only)
    @PostMapping("/details")
    public ResponseEntity<?> getOrderById(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Long> body) {

        Long orderId = body.get("orderId");
        if (orderId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "orderId is required"));
        }

        try {
            Order order = orderService.getOrderById(orderId);

            // Authorization check
            boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
            boolean isOwner = order.getUserId().equals(userId);

            if (!isAdmin && !isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You are not allowed to view this order"));
            }

            OrderResponseDto response = orderMapper.toResponseDto(order);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }



    // Get user's order history
    @GetMapping("/history")
    public ResponseEntity<?> getUserOrders(
            @RequestHeader("X-User-Id") Long userId) {
        try {
            List<Order> orders = orderService.getUserOrders(userId);
            List<OrderResponseDto> response = orderMapper.toResponseDtoList(orders);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get all orders (ADMIN ONLY)
    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders(
            @RequestHeader("X-User-Role") String role) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Admins only"));
        }

        try {
            List<Order> orders = orderService.getAllOrders();
            List<OrderResponseDto> response = orderMapper.toResponseDtoList(orders);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    // Get orders by status (Admins see all, Users see only their own)
    @PostMapping("/status")
    public ResponseEntity<?> getOrdersByStatus(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Status is required"));
        }

        try {
            List<Order> orders;

            // Admins see all orders with the given status
            if ("ADMIN".equalsIgnoreCase(role)) {
                orders = orderService.getOrdersByStatus(status);
            }
            // Regular users see only their own orders with the given status
            else {
                orders = orderService.getUserOrdersByStatus(userId, status);
            }

            List<OrderResponseDto> response = orderMapper.toResponseDtoList(orders);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }


    // Update order status (ADMIN ONLY)
    @PostMapping("/status/update")
    public ResponseEntity<?> updateOrderStatus(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody UpdateStatusRequestDto request) {

        // Admin check
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Admins only"));
        }

        try {
            Order order = orderService.updateOrderStatus(
                    request.getOrderId(),
                    request.getStatus()
            );
            OrderResponseDto response = orderMapper.toResponseDto(order);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }



    // Cancel order (OWNER or ADMIN only)
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelOrder(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Object> body) {

        Object orderIdObj = body.get("orderId");

        if (orderIdObj == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "orderId is required"));
        }

        Long orderId;
        try {
            orderId = orderIdObj instanceof Number ?
                    ((Number) orderIdObj).longValue() :
                    Long.parseLong(orderIdObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "orderId must be a valid number"));
        }

        try {
            Order order = orderService.getOrderById(orderId);

            // Authorization check
            boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
            boolean isOwner = order.getUserId().equals(userId);

            if (!isAdmin && !isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You are not allowed to cancel this order"));
            }

            orderService.cancelOrder(orderId);
            return ResponseEntity.ok(Map.of("message", "Order cancelled successfully"));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }


}