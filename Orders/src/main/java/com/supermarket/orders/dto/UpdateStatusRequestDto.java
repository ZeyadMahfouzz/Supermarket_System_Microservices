package com.supermarket.orders.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequestDto {
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PENDING|SHIPPING|DELIVERED|CANCELLED", 
             message = "Status must be PENDING, SHIPPING, DELIVERED, or CANCELLED")
    private String status;
}
