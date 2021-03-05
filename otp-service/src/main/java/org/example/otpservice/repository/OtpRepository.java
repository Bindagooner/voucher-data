package org.example.otpservice.repository;

import org.example.otpservice.entity.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpEntity, Long> {
    Optional<OtpEntity> findByReferenceNumber(String refNo);
}
