package com.tn.grocery_app.controller;

import com.tn.grocery_app.dto.*;
import com.tn.grocery_app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        String message = userService.register(request);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/send-otp-registration")
    public ResponseEntity<OtpResponse> sendOtpForRegistration(@RequestParam String phone) {
        OtpResponse response = userService.sendOtpForRegistration(phone);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp-registration")
    public ResponseEntity<String> verifyOtpForRegistration(@Valid @RequestBody OtpRequest request) {
        String message = userService.verifyOtpForRegistration(request);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forget-password")
    public ResponseEntity<OtpResponse> forgetPassword(@Valid @RequestBody ForgetPasswordRequest request) {
        OtpResponse response = userService.forgetPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String message = userService.resetPassword(request);
        return ResponseEntity.ok(message);
    }
}
