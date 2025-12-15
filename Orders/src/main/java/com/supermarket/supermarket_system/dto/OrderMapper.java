package com.supermarket.supermarket_system.dto;

import com.supermarket.supermarket_system.model.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    /**
     * Convert Order entity to OrderResponseDto
     */
    public OrderResponseDto toResponseDto(Order order) {
        return OrderResponseDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .items(order.getItems())
                .itemDetails(order.getItemDetails())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .totalAmount(calculateTotalAmount(order.getItemDetails()))
                .build();
    }

    /**
     * Convert list of Order entities to list of OrderResponseDto
     */
    public List<OrderResponseDto> toResponseDtoList(List<Order> orders) {
        return orders.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Calculate total amount from item details
     */
    private BigDecimal calculateTotalAmount(Map<String, Object> itemDetails) {
        if (itemDetails == null || itemDetails.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Try to extract total from itemDetails
        Object total = itemDetails.get("total");
        if (total instanceof BigDecimal) {
            return (BigDecimal) total;
        } else if (total instanceof Number) {
            return BigDecimal.valueOf(((Number) total).doubleValue());
        }

        // If no total, calculate from subtotals
        return itemDetails.values().stream()
                .filter(value -> value instanceof Map)
                .map(value -> {
                    Map<String, Object> details = (Map<String, Object>) value;
                    Object subtotal = details.get("subtotal");
                    if (subtotal instanceof BigDecimal) {
                        return (BigDecimal) subtotal;
                    } else if (subtotal instanceof Number) {
                        return BigDecimal.valueOf(((Number) subtotal).doubleValue());
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

