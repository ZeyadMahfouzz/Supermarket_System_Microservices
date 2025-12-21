package com.supermarket.supermarket_system.dto.payment;

import com.supermarket.supermarket_system.models.PaymentStatus;

public class CheckoutResponseDto {

    private String message;
    private Long orderId;
    private Long paymentId;
    private String transactionId;
    private PaymentStatus paymentStatus;
    private Double totalAmount;

    // Constructors
    public CheckoutResponseDto() {
    }

    public CheckoutResponseDto(String message, Long orderId, Long paymentId,
                               String transactionId, PaymentStatus paymentStatus, Double totalAmount) {
        this.message = message;
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.paymentStatus = paymentStatus;
        this.totalAmount = totalAmount;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
}