package org.example.voucher.dto;


public enum OrderStatus {

    PAYMENT_SUCCESS("Payment has been done"),
    VOUCHER_REQUESTING("Voucher is being requested from 3rd party"),
    SENDING_FAILED("Failed to sending sms the voucher code to customer"),
    COMPLETED("Voucher code was sent to customer successfully"),
    VOUCHER_REQUEST_FAILED("Voucher requesting was failed from 3rd party");

    private String description;
    OrderStatus(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
}
