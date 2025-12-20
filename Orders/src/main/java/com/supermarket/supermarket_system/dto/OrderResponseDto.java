package com.supermarket.supermarket_system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.supermarket.supermarket_system.dto.cart.ItemDetailsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {

    private Long id;
    private Long userId;

    // Items map: itemId (as String) -> quantity
    private Map<String, Integer> items;

    // Item details: itemId (as String) -> ItemDetailsDto (name, image, price, etc.)
    private Map<String, ItemDetailsDto> itemDetails;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime orderDate;

    private String status;
    private String paymentMethod;
    private BigDecimal totalAmount;
}