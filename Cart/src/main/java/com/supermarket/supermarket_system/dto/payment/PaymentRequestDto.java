package com.supermarket.supermarket_system.dto.payment;

import com.supermarket.supermarket_system.models.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PaymentRequestDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Valid
    private CreditCardPaymentDto creditCardPayment;

    @Valid
    private DebitCardPaymentDto debitCardPayment;

    @Valid
    private MobilePaymentDto mobilePayment;

    @Valid
    private BankTransferPaymentDto bankTransfer;

    private CashPaymentDto cashPayment;

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public CreditCardPaymentDto getCreditCardPayment() {
        return creditCardPayment;
    }

    public void setCreditCardPayment(CreditCardPaymentDto creditCardPayment) {
        this.creditCardPayment = creditCardPayment;
    }

    public DebitCardPaymentDto getDebitCardPayment() {
        return debitCardPayment;
    }

    public void setDebitCardPayment(DebitCardPaymentDto debitCardPayment) {
        this.debitCardPayment = debitCardPayment;
    }

    public MobilePaymentDto getMobilePayment() {
        return mobilePayment;
    }

    public void setMobilePayment(MobilePaymentDto mobilePayment) {
        this.mobilePayment = mobilePayment;
    }

    public BankTransferPaymentDto getBankTransfer() {
        return bankTransfer;
    }

    public void setBankTransfer(BankTransferPaymentDto bankTransfer) {
        this.bankTransfer = bankTransfer;
    }

    public CashPaymentDto getCashPayment() {
        return cashPayment;
    }

    public void setCashPayment(CashPaymentDto cashPayment) {
        this.cashPayment = cashPayment;
    }
}