package com.supermarket.supermarket_system.repository;

import com.supermarket.supermarket_system.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find all payments for a specific order
    List<Payment> findByOrderId(Long orderId);

    // Find payment by transaction ID
    Optional<Payment> findByTransactionId(String transactionId);

    // Find payments by status
    List<Payment> findByStatus(String status);

    // Find payments by payment method
    List<Payment> findByPaymentMethod(String paymentMethod);
}