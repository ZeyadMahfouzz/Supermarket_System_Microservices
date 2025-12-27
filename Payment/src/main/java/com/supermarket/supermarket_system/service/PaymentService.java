package com.supermarket.supermarket_system.service;

import com.supermarket.supermarket_system.dto.PaymentRequestDto;
import com.supermarket.supermarket_system.model.Payment;
import com.supermarket.supermarket_system.model.PaymentStatus;
import com.supermarket.supermarket_system.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service layer responsible for handling all payment-related business logic.
 * This class validates payment details, processes payments, retrieves payments,
 * and handles refunds.
 */
@Service
public class PaymentService {

    /**
     * Repository used to interact with the payments table in the database.
     */
    @Autowired
    private PaymentRepository paymentRepository;

    /**
     * Processes a payment request that contains detailed payment method information.
     *
     * @param request PaymentRequestDto containing user, order, amount, and payment details
     * @return the saved Payment entity with COMPLETED status
     */
    public Payment processPaymentWithDetails(PaymentRequestDto request) {

        // Validate payment method-specific details before processing
        validatePaymentMethodDetails(request);

        // Create a new Payment entity
        Payment payment = new Payment();
        payment.setUserId(request.getUserId());
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());

        // Generate a unique transaction ID
        payment.setTransactionId(UUID.randomUUID().toString());

        // All valid payments are instantly approved and marked as COMPLETED
        payment.setStatus(PaymentStatus.COMPLETED);

        // Persist payment to the database
        return paymentRepository.save(payment);
    }

    /**
     * Validates payment details based on the selected payment method.
     * Ensures required payment-specific data is present.
     *
     * @param request PaymentRequestDto containing payment method details
     */
    private void validatePaymentMethodDetails(PaymentRequestDto request) {

        switch (request.getPaymentMethod()) {

            case CREDIT_CARD:
                // Ensure credit card details exist
                if (request.getCreditCardPayment() == null) {
                    throw new IllegalArgumentException("Credit card payment details are required");
                }
                // Field-level validation handled by DTO annotations
                break;

            case DEBIT_CARD:
                // Ensure debit card details exist
                if (request.getDebitCardPayment() == null) {
                    throw new IllegalArgumentException("Debit card payment details are required");
                }
                break;

            case MOBILE_PAYMENT:
                // Ensure mobile payment details exist
                if (request.getMobilePayment() == null) {
                    throw new IllegalArgumentException("Mobile payment details are required");
                }
                break;

            case BANK_TRANSFER:
                // Ensure bank transfer details exist
                if (request.getBankTransfer() == null) {
                    throw new IllegalArgumentException("Bank transfer details are required");
                }
                break;

            case CASH:
                // Ensure cash payment details exist
                if (request.getCashPayment() == null) {
                    throw new IllegalArgumentException("Cash payment details are required");
                }

                // Cash payments must be explicitly confirmed
                Boolean confirmed = request.getCashPayment().getConfirmed();
                if (confirmed == null || !confirmed) {
                    throw new IllegalArgumentException("Cash payment must be confirmed");
                }
                break;

            default:
                // Handle unsupported or invalid payment methods
                throw new IllegalArgumentException("Invalid payment method");
        }
    }

    /**
     * Legacy payment processing method that accepts a Payment entity directly.
     *
     * @param payment Payment entity
     * @return saved Payment entity with COMPLETED status
     */
    public Payment processPayment(Payment payment) {

        // Generate a unique transaction ID
        payment.setTransactionId(UUID.randomUUID().toString());

        // Instantly approve payment
        payment.setStatus(PaymentStatus.COMPLETED);

        // Save payment to the database
        return paymentRepository.save(payment);
    }

    /**
     * Retrieves all payments from the database.
     *
     * @return list of all payments
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * Retrieves a payment by its database ID.
     *
     * @param id payment ID
     * @return Payment entity
     */
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found with id: " + id));
    }

    /**
     * Retrieves all payments associated with a specific order.
     *
     * @param orderId order ID
     * @return list of payments for the given order
     */
    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    /**
     * Retrieves all payments made by a specific user.
     *
     * @param userId user ID
     * @return list of payments for the given user
     */
    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    /**
     * Retrieves a payment using its transaction ID.
     *
     * @param transactionId unique transaction identifier
     * @return Payment entity
     */
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found with transaction id: " + transactionId));
    }

    /**
     * Refunds a completed payment by updating its status to REFUNDED.
     *
     * @param id payment ID
     * @return updated Payment entity
     */
    public Payment refundPayment(Long id) {

        // Retrieve payment or throw error if not found
        Payment payment = getPaymentById(id);

        // Only completed payments can be refunded
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException("Only completed payments can be refunded");
        }

        // Update payment status to REFUNDED
        payment.setStatus(PaymentStatus.REFUNDED);

        // Save updated payment
        return paymentRepository.save(payment);
    }
}
