package com.supermarket.supermarket_system.controller;

import com.supermarket.supermarket_system.model.Payment;
import com.supermarket.supermarket_system.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // Process a new payment
    @PostMapping
    public ResponseEntity<?> processPayment(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody Payment payment) {
        try {
            // Ensure the payment is for the requesting user
            payment.setUserId(userId);

            Payment processedPayment = paymentService.processPayment(payment);
            return ResponseEntity.status(HttpStatus.CREATED).body(processedPayment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process payment: " + e.getMessage()));
        }
    }

    // Get all payments (ADMIN ONLY)
    @GetMapping
    public ResponseEntity<?> getAllPayments(
            @RequestHeader("X-User-Role") String role) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Admins only"));
        }

        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    // Get payment by ID (OWNER or ADMIN only)
    @PostMapping("/details")
    public ResponseEntity<?> getPaymentById(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Long> body) {

        Long id = body.get("id");
        if (id == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "id is required"));
        }

        try {
            Payment payment = paymentService.getPaymentById(id);

            // Authorization check
            boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
            boolean isOwner = payment.getUserId() != null && payment.getUserId().equals(userId);

            if (!isAdmin && !isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You are not allowed to view this payment"));
            }

            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Payment not found"));
        }
    }

    // Get payments by order ID (OWNER or ADMIN only)
    @PostMapping("/order")
    public ResponseEntity<?> getPaymentsByOrderId(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Long> body) {

        Long orderId = body.get("orderId");
        if (orderId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "orderId is required"));
        }

        List<Payment> payments = paymentService.getPaymentsByOrderId(orderId);

        // For non-admins, filter to only show their own payments
        if (!"ADMIN".equalsIgnoreCase(role)) {
            payments = payments.stream()
                    .filter(p -> p.getUserId() != null && p.getUserId().equals(userId))
                    .toList();
        }

        return ResponseEntity.ok(payments);
    }

    // Get payment by transaction ID (OWNER or ADMIN only)
    @PostMapping("/transaction")
    public ResponseEntity<?> getPaymentByTransactionId(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, String> body) {

        String transactionId = body.get("transactionId");
        if (transactionId == null || transactionId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "transactionId is required"));
        }

        try {
            Payment payment = paymentService.getPaymentByTransactionId(transactionId);

            // Authorization check
            boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
            boolean isOwner = payment.getUserId() != null && payment.getUserId().equals(userId);

            if (!isAdmin && !isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You are not allowed to view this payment"));
            }

            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Payment not found"));
        }
    }

    // Get user's payment history
    @GetMapping("/history")
    public ResponseEntity<?> getUserPayments(
            @RequestHeader("X-User-Id") Long userId) {
        try {
            List<Payment> payments = paymentService.getPaymentsByUserId(userId);
            return ResponseEntity.ok(payments);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Refund a payment (ADMIN ONLY)
    @PostMapping("/refund")
    public ResponseEntity<?> refundPayment(
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Long> body) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Admins only"));
        }

        Long id = body.get("id");
        if (id == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "id is required"));
        }

        try {
            Payment refundedPayment = paymentService.refundPayment(id);
            return ResponseEntity.ok(refundedPayment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment Service is running!");
    }
}