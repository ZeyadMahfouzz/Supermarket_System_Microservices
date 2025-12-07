package com.supermarket.supermarket_system.repository;

import com.supermarket.supermarket_system.model.PaymentApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentApplication, Long> {

    PaymentApplication findByTransactionId(String transactionId);

}
