package com.tn.grocery_app.service;

import com.tn.grocery_app.entity.OtpVerificationEntity;
import com.tn.grocery_app.entity.SessionEntity;
import com.tn.grocery_app.entity.UserEntity;
import com.tn.grocery_app.repository.OtpVerificationRepository;
import com.tn.grocery_app.repository.SessionRepository;
import com.tn.grocery_app.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class AuthService {
    private final UserRepository userRepository;
    private final OtpVerificationRepository otpRepository;
    private final SessionRepository sessionRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       OtpVerificationRepository otpRepository,
                       SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // 1️⃣ Register (email → send OTP)
    public UserEntity registerEmail(String email) {

        Optional<UserEntity> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            createOtp(existingUser.get(), OtpVerificationEntity.OtpType.EMAIL_VERIFICATION);
            return existingUser.get();
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        createOtp(user, OtpVerificationEntity.OtpType.EMAIL_VERIFICATION);

        return user;
    }

    // 2️⃣ Verify email OTP
    public boolean verifyEmailOtp(UUID userId, String otpCode) {

        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return false;
        return verifyOtp(userOpt.get(), otpCode, OtpVerificationEntity.OtpType.EMAIL_VERIFICATION);
    }

    // 3️⃣ Set password (after verification)
    public boolean setPassword(UUID userId, String plainPassword) {

        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return false;

        UserEntity user = userOpt.get();

        if (!user.isEmailVerified()) return false;

        user.setHashedPassword(passwordEncoder.encode(plainPassword));
        userRepository.save(user);

        return true;
    }

    // 4️⃣ Login
    public Optional<SessionEntity> login(String email,
                                         String plainPassword,
                                         String deviceInfo,
                                         String ipAddress) {

        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return Optional.empty();

        UserEntity user = userOpt.get();

        if (!user.isEmailVerified()) return Optional.empty();
        if (user.getHashedPassword() == null) return Optional.empty();
        if (!passwordEncoder.matches(plainPassword, user.getHashedPassword())) return Optional.empty();

        SessionEntity session = new SessionEntity();
        session.setUser(user);
        session.setAccessToken(UUID.randomUUID().toString());
        session.setRefreshToken(UUID.randomUUID().toString());
        session.setDeviceInfo(deviceInfo);
        session.setIpAddress(ipAddress);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusDays(7));

        sessionRepository.save(session);

        return Optional.of(session);
    }

    // 5️⃣ Forgot password (send OTP)
    public boolean requestForgotPassword(String email) {

        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        createOtp(userOpt.get(), OtpVerificationEntity.OtpType.FORGOT_PASSWORD);
        return true;
    }

    // 6️⃣ Verify forgot password OTP
    public boolean verifyForgotPasswordOtp(UUID userId, String otpCode) {

        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return false;

        return verifyOtp(userOpt.get(), otpCode, OtpVerificationEntity.OtpType.FORGOT_PASSWORD);
    }

    // 7️⃣ Reset password
    public boolean resetPassword(UUID userId, String newPassword) {

        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return false;

        UserEntity user = userOpt.get();

        user.setHashedPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return true;
    }

    // =========================
    // 🔧 INTERNAL HELPERS
    // =========================

    private void createOtp(UserEntity user, OtpVerificationEntity.OtpType type) {

        String otpCode = String.format("%06d",
                ThreadLocalRandom.current().nextInt(0, 1000000));

        OtpVerificationEntity otp = new OtpVerificationEntity();
        otp.setUser(user);
        otp.setOtpCode(otpCode);
        otp.setType(type);
        otp.setUsed(false);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otp.setCreatedAt(LocalDateTime.now());

        otpRepository.save(otp);

        // TODO: integrate email sending service here
    }

    private boolean verifyOtp(UserEntity user,
                              String code,
                              OtpVerificationEntity.OtpType type) {

        Optional<OtpVerificationEntity> otpOpt =
                otpRepository.findTopByUserAndOtpCodeAndTypeAndIsUsedFalseOrderByCreatedAtDesc(
                        user, code, type
                );

        if (otpOpt.isEmpty()) return false;

        OtpVerificationEntity otp = otpOpt.get();

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) return false;

        otp.setUsed(true);
        otpRepository.save(otp);

        if (type == OtpVerificationEntity.OtpType.EMAIL_VERIFICATION) {
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        return true;
    }
}
