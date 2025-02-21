package org.example.voucher.entity;

import lombok.Data;
import org.example.voucher.dto.OrderStatus;

import javax.persistence.*;

@Entity
@Table(name = "voucher")
@Data
public class Voucher extends Audit {

    @Id
    @Column(name = "order_id", unique = true, nullable = false)
    private String orderId;

    @Column(name = "voucher_code")
    private String voucherCode;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "order_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
}
