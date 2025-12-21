package com.supermarket.supermarket_system.dto.payment;

import com.supermarket.supermarket_system.models.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class CheckoutRequestDto {

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