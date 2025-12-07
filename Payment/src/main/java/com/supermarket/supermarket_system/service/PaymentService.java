package com.supermarket.supermarket_system.service;

import com.supermarket.supermarket_system.model.PaymentApplication;
import com.supermarket.supermarket_system.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentApplication createPayment(PaymentApplication payment) {
        return paymentRepository.save(payment);
    }

    public List<PaymentApplication> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Optional<PaymentApplication> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public PaymentApplication updatePaymentStatus(Long id, String status) {
        Optional<PaymentApplication> optionalPayment = paymentRepository.findById(id);
        if (optionalPayment.isEmpty()) {
            return null;
        }
        PaymentApplication payment = optionalPayment.get();
        payment.setStatus(status);
        return paymentRepository.save(payment);
    }

    public boolean deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            return false;
        }
        paymentRepository.deleteById(id);
        return true;
    }

    public PaymentApplication getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }
}
