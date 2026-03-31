package com.tn.grocery_app.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "otp_verifications")
@Data
public class OtpVerificationEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "otp_code", length = 6)
    private String otpCode;

    @Enumerated(EnumType.STRING)
    private OtpType type;

    @Column(name = "is_used")
    private boolean isUsed;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum OtpType {
        EMAIL_VERIFICATION,
        FORGOT_PASSWORD
    }
}