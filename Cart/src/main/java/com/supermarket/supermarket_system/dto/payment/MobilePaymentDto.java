package com.supermarket.supermarket_system.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MobilePaymentDto {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9][0-9]{7,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "Provider is required")
    private String provider; // e.g., "Apple Pay", "Google Pay", "PayPal", etc.

    // Getters and Setters
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}