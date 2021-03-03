package org.example.voucher.repository;

import org.example.voucher.dto.OrderStatus;
import org.example.voucher.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByOrderId(String orderId);
    List<Order> findByPhoneNumber(String phoneNumber);
    List<Order> findByOrderStatus(OrderStatus orderStatus);
}
