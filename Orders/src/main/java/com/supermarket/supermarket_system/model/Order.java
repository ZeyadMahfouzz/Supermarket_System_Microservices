package com.supermarket.supermarket_system.model;

import com.supermarket.supermarket_system.dto.cart.ItemDetailsDto;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Single field for item details: itemId (as String) -> ItemDetailsDto
    @Convert(converter = ItemsMapConverter.class)
    @Column(name = "item_details", columnDefinition = "TEXT")
    private Map<String, ItemDetailsDto> itemDetails = new HashMap<>();

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false)
    private String status; // e.g., PENDING, COMPLETED, CANCELLED, SHIPPED

    @Column(nullable = false)
    private String paymentMethod; // e.g., CREDIT_CARD, CASH, etc.

    @Column(name = "total_amount")
    private Double totalAmount;

    public Order() {
        this.orderDate = LocalDateTime.now();
        this.status = "PENDING";
        this.paymentMethod = "UNSPECIFIED";
        this.totalAmount = 0.0;
    }

}