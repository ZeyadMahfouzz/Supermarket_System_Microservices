package com.supermarket.orders.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private Long id;
    private Long userId;
    private Map<Long, Integer> items;
    private Map<String, Object> itemDetails;
    private LocalDateTime orderDate;
    private String status;
    private String paymentMethod;
    private BigDecimal totalAmount;
}
