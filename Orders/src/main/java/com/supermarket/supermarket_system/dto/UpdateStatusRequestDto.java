package com.supermarket.supermarket_system.dto;

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
    @Pattern(regexp = "PENDING|PROCESSING|SHIPPING|DELIVERED|CANCELLED",
             message = "Status must be PENDING, PROCESSING, SHIPPING, DELIVERED, or CANCELLED")
    private String status;
}
