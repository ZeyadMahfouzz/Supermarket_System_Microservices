/**
 * PaymentController.java
 *
 * PURPOSE:
 * This is the API layer (Controller) that exposes REST endpoints for payment operations.
 * It handles HTTP requests, validates user permissions, and delegates business logic to PaymentService.
 *
 * SECURITY:
 * Uses header-based authentication with X-User-Id and X-User-Role headers.
 * Implements role-based access control (RBAC) with ADMIN and regular user roles.
 */
package com.supermarket.supermarket_system.controller;

import com.supermarket.supermarket_system.dto.PaymentRequestDto;
import com.supermarket.supermarket_system.dto.PaymentResponseDto;
import com.supermarket.supermarket_system.model.Payment;
import com.supermarket.supermarket_system.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Marks this class as a REST controller that handles HTTP requests
@RestController
// Base URL path for all endpoints in this controller: /payment
@RequestMapping("/payment")
public class PaymentController {

    // Dependency injection: Spring automatically provides PaymentService instance
    @Autowired
    private PaymentService paymentService;

    /**
     * ENDPOINT: POST /payment/process
     * PURPOSE: Process a new payment transaction
     *
     * SECURITY:
     * - Requires X-User-Id header to identify the requesting user
     * - Validates that the payment userId matches the requesting user (prevents payment fraud)
     *
     * REQUEST BODY: PaymentRequestDto with payment details
     * RESPONSE: PaymentResponseDto with transaction details on success
     *
     * ERROR HANDLING:
     * - 403 FORBIDDEN: User ID mismatch (user trying to pay for someone else)
     * - 400 BAD REQUEST: Invalid payment data (validation failures)
     * - 500 INTERNAL SERVER ERROR: Unexpected processing errors
     */
    @PostMapping("/process")
    public ResponseEntity<?> processPaymentFromCart(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody PaymentRequestDto request) {
        try {
            // SECURITY CHECK: Ensure the payment is for the requesting user
            // This prevents User A from creating payments on behalf of User B
            if (!request.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "User ID mismatch"));
            }

            // Delegate payment processing to service layer
            Payment payment = paymentService.processPaymentWithDetails(request);

            // Build response DTO with essential payment information
            PaymentResponseDto response = new PaymentResponseDto(
                    payment.getId(),
                    payment.getTransactionId(),
                    payment.getStatus(),
                    "Payment processed successfully"
            );

            // Return 201 CREATED with payment details
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // Handle validation errors (e.g., missing payment method details)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Catch-all for unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process payment: " + e.getMessage()));
        }
    }

    /**
     * ENDPOINT: GET /payment
     * PURPOSE: Retrieve all payments in the system
     *
     * SECURITY: ADMIN ONLY
     * - Requires X-User-Role header with "ADMIN" value
     * - Regular users cannot access this endpoint
     *
     * USE CASE: Administrative reporting, auditing, financial reconciliation
     *
     * RESPONSE: List of all Payment objects
     * ERROR: 403 FORBIDDEN if not admin
     */
    @GetMapping
    public ResponseEntity<?> getAllPayments(
            @RequestHeader("X-User-Role") String role) {

        // AUTHORIZATION CHECK: Only admins can view all payments
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Admins only"));
        }

        // Retrieve all payments from database
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    /**
     * ENDPOINT: POST /payment/details
     * PURPOSE: Get a specific payment by ID
     *
     * SECURITY: OWNER or ADMIN only
     * - Users can only view their own payments
     * - Admins can view any payment
     *
     * REQUEST BODY: {"id": <payment_id>}
     *
     * AUTHORIZATION LOGIC:
     * 1. Check if requester is ADMIN → allow access
     * 2. Check if requester is payment owner → allow access
     * 3. Otherwise, → deny access (403 FORBIDDEN)
     */
    @PostMapping("/details")
    public ResponseEntity<?> getPaymentById(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Long> body) {

        // Extract payment ID from request body
        Long id = body.get("id");
        if (id == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "id is required"));
        }

        try {
            // Fetch payment from database
            Payment payment = paymentService.getPaymentById(id);

            // AUTHORIZATION CHECK: Is user authorized to view this payment?
            boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
            boolean isOwner = payment.getUserId() != null && payment.getUserId().equals(userId);

            // Deny access if user is neither admin nor owner
            if (!isAdmin && !isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You are not allowed to view this payment"));
            }

            // User is authorized, return payment details
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            // Payment not found in database
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Payment not found"));
        }
    }

    /**
     * ENDPOINT: POST /payment/order
     * PURPOSE: Get all payments associated with a specific order
     *
     * SECURITY: OWNER or ADMIN
     * - Regular users see only their own payments for the order
     * - Admins see all payments for the order
     *
     * REQUEST BODY: {"orderId": <order_id>}
     *
     * USE CASE: Track multiple payment attempts for an order, or split payments
     */
    @PostMapping("/order")
    public ResponseEntity<?> getPaymentsByOrderId(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Long> body) {

        // Extract order ID from request body
        Long orderId = body.get("orderId");
        if (orderId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "orderId is required"));
        }

        // Fetch all payments for this order
        List<Payment> payments = paymentService.getPaymentsByOrderId(orderId);

        // AUTHORIZATION: Filter results for non-admins
        // Regular users only see their own payments, even if multiple users paid for same order
        if (!"ADMIN".equalsIgnoreCase(role)) {
            payments = payments.stream()
                    .filter(p -> p.getUserId() != null && p.getUserId().equals(userId))
                    .toList();
        }

        return ResponseEntity.ok(payments);
    }

    /**
     * ENDPOINT: POST /payment/transaction
     * PURPOSE: Get payment by unique transaction ID
     *
     * SECURITY: OWNER or ADMIN only
     *
     * REQUEST BODY: {"transactionId": "<transaction_id>"}
     *
     * USE CASE:
     * - Customer support looking up transactions
     * - Payment reconciliation with external payment gateways
     * - Dispute resolution
     */
    @PostMapping("/transaction")
    public ResponseEntity<?> getPaymentByTransactionId(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, String> body) {

        // Extract and validate transaction ID
        String transactionId = body.get("transactionId");
        if (transactionId == null || transactionId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "transactionId is required"));
        }

        try {
            // Lookup payment by transaction ID
            Payment payment = paymentService.getPaymentByTransactionId(transactionId);

            // AUTHORIZATION CHECK: Admin or payment owner only
            boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
            boolean isOwner = payment.getUserId() != null && payment.getUserId().equals(userId);

            if (!isAdmin && !isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You are not allowed to view this payment"));
            }

            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            // Transaction ID not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Payment not found"));
        }
    }

    /**
     * ENDPOINT: GET /payment/history
     * PURPOSE: Get payment history for the requesting user
     *
     * SECURITY: User can only see their own payment history
     *
     * USE CASE:
     * - Customer viewing their past transactions
     * - Personal finance tracking
     * - Receipt retrieval
     */
    @GetMapping("/history")
    public ResponseEntity<?> getUserPayments(
            @RequestHeader("X-User-Id") Long userId) {
        try {
            // Fetch all payments for this user
            List<Payment> payments = paymentService.getPaymentsByUserId(userId);
            return ResponseEntity.ok(payments);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ENDPOINT: POST /payment/refund
     * PURPOSE: Refund a completed payment
     *
     * SECURITY: ADMIN ONLY
     * - Only admins can issue refunds
     * - This is a sensitive financial operation
     *
     * REQUEST BODY: {"id": <payment_id>}
     *
     * BUSINESS RULE: Only COMPLETED payments can be refunded
     *
     * USE CASE:
     * - Customer returns items
     * - Payment dispute resolution
     * - Erroneous charges
     */
    @PostMapping("/refund")
    public ResponseEntity<?> refundPayment(
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Long> body) {

        // AUTHORIZATION: Admin-only operation
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Admins only"));
        }

        // Extract payment ID
        Long id = body.get("id");
        if (id == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "id is required"));
        }

        try {
            // Process refund through service layer
            Payment refundedPayment = paymentService.refundPayment(id);
            return ResponseEntity.ok(refundedPayment);
        } catch (RuntimeException e) {
            // Refund failed (e.g., payment not completed, already refunded)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ENDPOINT: GET /payment/health
     * PURPOSE: Health check endpoint for monitoring
     *
     * SECURITY: Public (no authentication required)
     *
     * USE CASE:
     * - Kubernetes/Docker health probes
     * - Load balancer health checks
     * - Monitoring systems (Prometheus, Grafana)
     * - Service discovery confirmation
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment Service is running!");
    }
}