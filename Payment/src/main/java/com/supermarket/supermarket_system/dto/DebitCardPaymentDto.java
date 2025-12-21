package com.supermarket.supermarket_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DebitCardPaymentDto {

    @NotBlank(message = "Card number is required")
    @Size(min = 13, max = 19, message = "Card number must be between 13 and 19 digits")
    @Pattern(regexp = "^[0-9]+$", message = "Card number must contain only digits")
    private String cardNumber;

    @NotBlank(message = "Cardholder name is required")
    private String cardholderName;

    @NotBlank(message = "Expiry date is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Expiry date must be in MM/YY format")
    private String expiryDate;

    @NotBlank(message = "CVV is required")
    @Size(min = 3, max = 3, message = "CVV must be exactly 3 digits")
    @Pattern(regexp = "^[0-9]{3}$", message = "CVV must be 3 digits")
    private String cvv;

    // Getters and Setters
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}