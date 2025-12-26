package com.supermarket.supermarket_system.service;

import com.supermarket.supermarket_system.dto.PaymentRequestDto;
import com.supermarket.supermarket_system.model.Payment;
import com.supermarket.supermarket_system.model.PaymentStatus;
import com.supermarket.supermarket_system.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    // Process payment with detailed payment method information
    public Payment processPaymentWithDetails(PaymentRequestDto request) {
        // Validate payment method specific data
        validatePaymentMethodDetails(request);

        // Create payment entity
        Payment payment = new Payment();
        payment.setUserId(request.getUserId());
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTransactionId(UUID.randomUUID().toString());

        // Instant approval - all valid payments are COMPLETED immediately
        payment.setStatus(PaymentStatus.COMPLETED);

        return paymentRepository.save(payment);
    }

    private void validatePaymentMethodDetails(PaymentRequestDto request) {
        switch (request.getPaymentMethod()) {
            case CREDIT_CARD:
                if (request.getCreditCardPayment() == null) {
                    throw new IllegalArgumentException("Credit card payment details are required");
                }
                // All required fields are validated by DTO annotations
                break;
            case DEBIT_CARD:
                if (request.getDebitCardPayment() == null) {
                    throw new IllegalArgumentException("Debit card payment details are required");
                }
                // All required fields are validated by DTO annotations
                break;
            case MOBILE_PAYMENT:
                if (request.getMobilePayment() == null) {
                    throw new IllegalArgumentException("Mobile payment details are required");
                }
                // All required fields are validated by DTO annotations
                break;
            case BANK_TRANSFER:
                if (request.getBankTransfer() == null) {
                    throw new IllegalArgumentException("Bank transfer details are required");
                }
                // All required fields are validated by DTO annotations
                break;
            case CASH:
                if (request.getCashPayment() == null) {
                    throw new IllegalArgumentException("Cash payment details are required");
                }
                // Check if confirmed is null or false
                Boolean confirmed = request.getCashPayment().getConfirmed();
                if (confirmed == null || !confirmed) {
                    throw new IllegalArgumentException("Cash payment must be confirmed");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid payment method");
        }
    }

    // Process payment (legacy method)
    public Payment processPayment(Payment payment) {
        // Generate a unique transaction ID
        payment.setTransactionId(UUID.randomUUID().toString());

        // Instant approval
        payment.setStatus(PaymentStatus.COMPLETED);

        return paymentRepository.save(payment);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found with transaction id: " + transactionId));
    }

    public Payment refundPayment(Long id) {
        Payment payment = getPaymentById(id);

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException("Only completed payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        return paymentRepository.save(payment);
    }
}