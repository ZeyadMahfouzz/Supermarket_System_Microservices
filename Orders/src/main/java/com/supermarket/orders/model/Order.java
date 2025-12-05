package com.supermarket.orders.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Import User and ItemsMapConverter if available
// import com.supermarket.orders.model.User;
// import com.supermarket.orders.model.ItemsMapConverter;

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

    @Convert(converter = ItemsMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<Long, Integer> items = new HashMap<>(); // itemId -> quantity

    @Transient // Not persisted to database, used only for API responses
    private Map<String, Object> itemDetails = new HashMap<>(); // itemName -> {quantity, price, subtotal}

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false)
    private String status; // e.g., SHIPPING, COMPLETED, CANCELLED, SHIPPED

    @Column(nullable = false)
    private String paymentMethod; // e.g., CREDIT_CARD, PAYPAL, CASH_ON_DELIVERY

    public Order() {
        this.orderDate = LocalDateTime.now();
        this.status = "PENDING";
        this.paymentMethod = "UNSPECIFIED";
    }


    public Order(Long userId, Map<Long, Integer> items) {
        this.userId = userId;
        this.items = items;
        this.orderDate = LocalDateTime.now();
        this.status = "SHIPPING";
    }


    // Calculated total - requires ItemRepository to fetch prices
    public Double getTotal(Map<Long, Double> itemPrices) {
        return items.entrySet().stream()
                .mapToDouble(entry -> {
                    Long itemId = entry.getKey();
                    Integer quantity = entry.getValue();
                    Double price = itemPrices.getOrDefault(itemId, 0.0);
                    return price * quantity;
                })
                .sum();
    }
}