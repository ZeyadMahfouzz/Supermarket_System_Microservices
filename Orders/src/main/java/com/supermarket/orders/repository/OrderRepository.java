package com.supermarket.orders.repository;

import com.supermarket.orders.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(String status);
    List<Order> findByUserIdAndStatus(Long userId, String status);
    // Sorted methods
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
    List<Order> findByStatusOrderByOrderDateDesc(String status);
    List<Order> findByUserIdAndStatusOrderByOrderDateDesc(Long userId, String status);
    List<Order> findAllByOrderByOrderDateDesc();
}
