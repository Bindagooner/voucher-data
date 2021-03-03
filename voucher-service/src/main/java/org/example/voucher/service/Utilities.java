package org.example.voucher.service;

import org.example.voucher.dto.OrderStatus;
import org.example.voucher.entity.Order;

public class Utilities {

    public static Order buildOrder(String orderId, String voucher, String phoneNumber, OrderStatus status) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setPhoneNumber(phoneNumber);
        order.setVoucherCode(voucher);
        order.setOrderStatus(status);
        return order;
    }

}
