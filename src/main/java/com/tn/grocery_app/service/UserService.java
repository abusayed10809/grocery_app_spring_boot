package com.tn.grocery_app.service;

import com.tn.grocery_app.dto.*;
import com.tn.grocery_app.entity.UserEntity;
import com.tn.grocery_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;

    public String register(RegisterRequest request) {
        Optional<UserEntity> existingUser = userRepository.findByPhone(request.getPhone());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User already exists");
        }
        UserEntity user = new UserEntity();
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setVerified(false);
        userRepository.save(user);
        return "User registered successfully. Please verify OTP.";
    }

    public OtpResponse sendOtpForRegistration(String phone) {
        UserEntity user = userRepository.findByPhone(phone).orElseThrow(() -> new RuntimeException("User not found"));
        if (user.isVerified()) {
            throw new RuntimeException("User already verified");
        }
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);
        return new OtpResponse(otp, "OTP sent successfully");
    }

    public String verifyOtpForRegistration(OtpRequest request) {
        UserEntity user = userRepository.findByPhone(request.getPhone()).orElseThrow(() -> new RuntimeException("User not found"));
        if (user.isVerified()) {
            throw new RuntimeException("User already verified");
        }
        if (!request.getOtp().equals(user.getOtp()) || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new RuntimeException("Invalid or expired OTP");
        }
        user.setVerified(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        return "User verified successfully";
    }

    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByPhone(request.getPhone()).orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isVerified()) {
            throw new RuntimeException("User not verified");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        String token = jwtUtil.generateToken(user.getPhone());
        return new AuthResponse(token, "Login successful");
    }

    public OtpResponse forgetPassword(ForgetPasswordRequest request) {
        UserEntity user = userRepository.findByPhone(request.getPhone()).orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isVerified()) {
            throw new RuntimeException("User not verified");
        }
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);
        return new OtpResponse(otp, "OTP sent for password reset");
    }

    public String resetPassword(ResetPasswordRequest request) {
        UserEntity user = userRepository.findByPhone(request.getPhone()).orElseThrow(() -> new RuntimeException("User not found"));
        if (!request.getOtp().equals(user.getOtp()) || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new RuntimeException("Invalid or expired OTP");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        return "Password reset successfully";
    }

    private String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }
}
