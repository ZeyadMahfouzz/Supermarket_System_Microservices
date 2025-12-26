package com.supermarket.supermarket_system.dto;

import com.supermarket.supermarket_system.model.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
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
                .itemDetails(order.getItemDetails())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .totalAmount(calculateTotalAmount(order))
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
     * Calculate total amount from order
     * Priority: 1) order.totalAmount, 2) sum of itemDetails subtotals, 3) zero
     */
    private BigDecimal calculateTotalAmount(Order order) {
        // First priority: use the totalAmount field if available
        if (order.getTotalAmount() != null && order.getTotalAmount() > 0) {
            return BigDecimal.valueOf(order.getTotalAmount());
        }

        // Second priority: calculate from itemDetails if available
        if (order.getItemDetails() != null && !order.getItemDetails().isEmpty()) {
            return order.getItemDetails().values().stream()
                    .filter(details -> details != null && details.getSubtotal() != null)
                    .map(details -> BigDecimal.valueOf(details.getSubtotal()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // Fallback: return zero
        return BigDecimal.ZERO;
    }
}