package org.example.voucher.repository;

import org.example.voucher.dto.OrderStatus;
import org.example.voucher.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Voucher, String> {
    Optional<Voucher> findByOrderId(String orderId);
    List<Voucher> findByPhoneNumber(String phoneNumber);
    List<Voucher> findByOrderStatus(OrderStatus orderStatus);
}
