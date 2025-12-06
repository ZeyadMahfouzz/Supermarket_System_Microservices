package com.supermarket.supermarket_system.service;

import com.supermarket.supermarket_system.model.Payment;
import com.supermarket.supermarket_system.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    // Process a new payment
    public Payment processPayment(Payment payment) {
        // Generate unique transaction ID
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString());

        // Simulate payment processing
        // In real application, integrate with payment gateway
        boolean paymentSuccess = simulatePaymentGateway(payment);

        if (paymentSuccess) {
            payment.setStatus("COMPLETED");
        } else {
            payment.setStatus("FAILED");
        }

        return paymentRepository.save(payment);
    }

    // Get all payments
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // Get payment by ID
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    // Get payments by order ID
    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    // Get payment by transaction ID
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found with transaction id: " + transactionId));
    }

    // Refund a payment
    public Payment refundPayment(Long paymentId) {
        Payment payment = getPaymentById(paymentId);

        if (!"COMPLETED".equals(payment.getStatus())) {
            throw new RuntimeException("Only completed payments can be refunded");
        }

        payment.setStatus("REFUNDED");
        return paymentRepository.save(payment);
    }

    // Simulate payment gateway (90% success rate)
    private boolean simulatePaymentGateway(Payment payment) {
        // Simulate processing delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 90% success rate
        return Math.random() > 0.1;
    }
}