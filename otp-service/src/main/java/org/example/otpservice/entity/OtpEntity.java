package org.example.otpservice.entity;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Table(name = "otp")
@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class OtpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator="native")
    @GenericGenerator(name="native", strategy="native")
    @Column(name = "id")
    private Long id;

    @Column(name = "phone_no")
    private String phoneNumber;

    @Column(name = "ref_no")
    private String referenceNumber;

    @Column(name = "code")
    private String code;

    @Column(name = "requester")
    private String requester;

    @Column(name = "is_validated")
    private boolean isValidated;

    @Column(name = "expired_time")
    private long expiredTime;

    @Column(name = "created_date")
    @CreatedDate
    private Date createdDate;
}
