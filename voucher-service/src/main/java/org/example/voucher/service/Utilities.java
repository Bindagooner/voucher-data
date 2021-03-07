package org.example.voucher.service;

import org.example.voucher.dto.OrderStatus;
import org.example.voucher.entity.Voucher;

public class Utilities {

    public static Voucher buildOrder(String orderId, String voucher, String phoneNumber, OrderStatus status) {
        Voucher order = new Voucher();
        order.setOrderId(orderId);
        order.setPhoneNumber(phoneNumber);
        order.setVoucherCode(voucher);
        order.setOrderStatus(status);
        return order;
    }

}
