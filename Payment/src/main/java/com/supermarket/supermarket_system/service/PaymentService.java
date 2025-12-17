package com.supermarket.supermarket_system.service;

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

    public Payment processPayment(Payment payment) {
        // Generate a unique transaction ID
        payment.setTransactionId(UUID.randomUUID().toString());

        // Simulate payment processing logic
        // In a real application, this would integrate with a payment gateway
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