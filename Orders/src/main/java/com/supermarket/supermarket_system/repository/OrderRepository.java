package com.supermarket.supermarket_system.repository;

import com.supermarket.supermarket_system.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Sorted methods
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
    List<Order> findByStatusOrderByOrderDateDesc(String status);
    List<Order> findByUserIdAndStatusOrderByOrderDateDesc(Long userId, String status);
    List<Order> findAllByOrderByOrderDateDesc();
}
