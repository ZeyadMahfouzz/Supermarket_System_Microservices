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

    // Changed to Map<String, Integer> to match CartCheckoutEvent
    @Convert(converter = ItemsMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Integer> items = new HashMap<>(); // itemId (as String) -> quantity

    // Changed to Map<String, ItemDetailsDto> and added converter
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

    public Order(Long userId, Map<String, Integer> items) {
        this.userId = userId;
        this.items = items;
        this.orderDate = LocalDateTime.now();
        this.status = "PENDING";
        this.totalAmount = 0.0;
    }

    // Calculate total from itemDetails
    public Double calculateTotal() {
        if (itemDetails == null || itemDetails.isEmpty()) {
            return totalAmount != null ? totalAmount : 0.0;
        }
        return itemDetails.values().stream()
                .mapToDouble(details -> details.getSubtotal() != null ? details.getSubtotal() : 0.0)
                .sum();
    }
}