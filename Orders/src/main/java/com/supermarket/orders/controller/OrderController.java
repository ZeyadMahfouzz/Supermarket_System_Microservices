package com.supermarket.orders.controller;

import com.supermarket.orders.dto.CheckoutRequestDto;
import com.supermarket.orders.dto.OrderMapper;
import com.supermarket.orders.dto.OrderResponseDto;
import com.supermarket.orders.dto.UpdateStatusRequestDto;
import com.supermarket.orders.model.Order;
import com.supermarket.orders.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/Orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    // Create order from user's cart
    @PostMapping("/{userId}/checkout")
    public ResponseEntity<?> createOrder(
            @PathVariable Long userId,
            @Valid @RequestBody CheckoutRequestDto request) {
        try {
            Order order = orderService.createOrderFromCart(userId, request.getPaymentMethod());
            OrderResponseDto response = orderMapper.toResponseDto(order);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get specific order by ID
    @GetMapping("/{orderId}/details")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            OrderResponseDto response = orderMapper.toResponseDto(order);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get user's order history
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<?> getUserOrders(@PathVariable Long userId) {
        try {
            List<Order> orders = orderService.getUserOrders(userId);
            List<OrderResponseDto> response = orderMapper.toResponseDtoList(orders);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get all orders (ADMIN ONLY - enforced by SecurityConfig)
    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            List<OrderResponseDto> response = orderMapper.toResponseDtoList(orders);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get orders by status (ADMIN ONLY - enforced by SecurityConfig)
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            List<Order> orders = orderService.getOrdersByStatus(status);
            List<OrderResponseDto> response = orderMapper.toResponseDtoList(orders);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get user's orders filtered by status
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<?> getUserOrdersByStatus(
            @PathVariable Long userId,
            @PathVariable String status) {
        try {
            List<Order> orders = orderService.getUserOrdersByStatus(userId, status);
            List<OrderResponseDto> response = orderMapper.toResponseDtoList(orders);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Update order status (ADMIN ONLY - enforced by SecurityConfig)
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateStatusRequestDto request) {
        try {
            Order order = orderService.updateOrderStatus(orderId, request.getStatus());
            OrderResponseDto response = orderMapper.toResponseDto(order);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Cancel order
    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok(Map.of("message", "Order cancelled successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}