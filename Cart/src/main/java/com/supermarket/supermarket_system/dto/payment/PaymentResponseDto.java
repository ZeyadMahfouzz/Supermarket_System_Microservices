package com.supermarket.supermarket_system.dto.payment;

import com.supermarket.supermarket_system.models.PaymentStatus;

public class PaymentResponseDto {

    private Long paymentId;
    private String transactionId;
    private PaymentStatus status;
    private String message;

    // Constructors
    public PaymentResponseDto() {
    }

    public PaymentResponseDto(Long paymentId, String transactionId, PaymentStatus status, String message) {
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
    }

    // Getters and Setters
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

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}