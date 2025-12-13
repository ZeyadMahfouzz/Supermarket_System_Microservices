package com.supermarket.orders.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequestDto {
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
}
