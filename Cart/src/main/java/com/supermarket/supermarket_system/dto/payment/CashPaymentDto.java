package com.supermarket.supermarket_system.dto.payment;

public class CashPaymentDto {

    private Boolean confirmed; // Remove @NotNull

    private String notes;

    // Getters and Setters
    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}