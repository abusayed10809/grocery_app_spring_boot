package com.tn.grocery_app.repository;

import com.tn.grocery_app.entity.OtpVerificationEntity;
import com.tn.grocery_app.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpVerificationRepository extends JpaRepository<OtpVerificationEntity, UUID> {
    Optional<OtpVerificationEntity> findTopByUserAndOtpCodeAndTypeAndIsUsedFalseOrderByCreatedAtDesc(UserEntity user, String otpCode, OtpVerificationEntity.OtpType type);
}
