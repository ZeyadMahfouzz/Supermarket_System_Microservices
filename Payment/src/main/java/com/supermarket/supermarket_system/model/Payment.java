// Domain model representing a payment record.
// This class is a JPA entity mapped to the "payments" table in the database.

package com.supermarket.supermarket_system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@Entity // Marks this class as a JPA entity (database table)
@Table(name = "payments") // Explicitly sets the table name in the database
public class Payment {

    // Primary key for the payments table
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID of the user who made the payment
    // Must always exist (cannot be null)
    @NotNull(message = "User ID is required")
    @Column(nullable = false, name = "user_id")
    private Long userId;

    // ID of the order this payment is associated with
    @NotNull(message = "Order ID is required")
    @Column(nullable = false)
    private Long orderId;

    // Total amount paid
    // Must be positive and non-null
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(nullable = false)
    private Double amount;

    // Payment method used (CREDIT_CARD, CASH, etc.)
    // Stored as STRING instead of ordinal for readability and safety
    @NotNull(message = "Payment method is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    // Current status of the payment (PENDING, COMPLETED, FAILED, REFUNDED)
    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    // Unique transaction identifier generated during payment processing
    // Used for tracking and external references
    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    // Timestamp when the payment record was created
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Timestamp of the last update to the payment record
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =========================
    // Constructors
    // =========================

    // Default constructor
    // Automatically initializes timestamps and sets status to PENDING
    public Payment() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
    }

    // Convenience constructor for quickly creating a payment
    // Used mainly when creating a new payment before persistence
    public Payment(Long userId, Long orderId, Double amount, PaymentMethod paymentMethod) {
        this(); // Calls the default constructor
        this.userId = userId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    // =========================
    // Getters and Setters
    // =========================

    // Returns the payment ID
    public Long getId() {
        return id;
    }

    // Sets the payment ID (usually handled by JPA)
    public void setId(Long id) {
        this.id = id;
    }

    // Returns the user ID associated with the payment
    public Long getUserId() {
        return userId;
    }

    // Sets the user ID
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // Returns the order ID linked to this payment
    public Long getOrderId() {
        return orderId;
    }

    // Sets the order ID
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    // Returns the payment amount
    public Double getAmount() {
        return amount;
    }

    // Sets the payment amount
    public void setAmount(Double amount) {
        this.amount = amount;
    }

    // Returns the payment method
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    // Sets the payment method
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // Returns the current payment status
    public PaymentStatus getStatus() {
        return status;
    }

    // Updates the payment status
    // Also refreshes the updatedAt timestamp
    public void setStatus(PaymentStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    // Returns the transaction ID
    public String getTransactionId() {
        return transactionId;
    }

    // Sets the transaction ID
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    // Returns creation timestamp
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Sets creation timestamp
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Returns last update timestamp
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Sets last update timestamp
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // =========================
    // JPA Lifecycle Callback
    // =========================

    // Automatically called before the entity is updated in the database
    // Ensures updatedAt always reflects the latest modification
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
