// DTO used to capture and validate credit card payment details.
// Validation annotations ensure incorrect or insecure data
// never reaches the service layer.

package com.supermarket.supermarket_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreditCardPaymentDto {

    // Primary card number (PAN)
    @NotBlank(message = "Card number is required")
    @Size(min = 13, max = 19, message = "Card number must be between 13 and 19 digits")
    @Pattern(regexp = "^[0-9]+$", message = "Card number must contain only digits")
    private String cardNumber;

    // Name printed on the card
    @NotBlank(message = "Cardholder name is required")
    private String cardholderName;

    // Expiry date in MM/YY format
    @NotBlank(message = "Expiry date is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Expiry date must be in MM/YY format")
    private String expiryDate;

    // Card Verification Value (security code)
    @NotBlank(message = "CVV is required")
    @Size(min = 3, max = 3, message = "CVV must be exactly 3 digits")
    @Pattern(regexp = "^[0-9]{3}$", message = "CVV must be 3 digits")
    private String cvv;

    // Standard getters and setters
}
