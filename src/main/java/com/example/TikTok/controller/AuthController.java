package com.example.TikTok.controller;

import com.example.TikTok.dto.request.*;
import com.example.TikTok.dto.response.AuthResponse;
import com.example.TikTok.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/google")
    public  ResponseEntity<AuthResponse> loginWithGoogle(@RequestBody GoogleLoginRequest request){
        return  ResponseEntity.ok(authService.loginWithGoogle(request));
    }
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request){
        return ResponseEntity.ok(authService.register(request));
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request){
        AuthResponse response = authService.login(request);


        log.info("🔑 Login thành công! Token sinh ra là: {}", response.getToken());


        return ResponseEntity.ok(response);
    }
    @PostMapping("/forgot-password")
    public  ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request){
        String result=authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(result);
    }
    @PostMapping("/reset-password")
    public  ResponseEntity<String>  resetPassword(@RequestBody ResetPasswordRequest request){
        String result=authService.resetPassword(request);
        return ResponseEntity.ok(result);
    }
    @PostMapping("/refresh-token")
    public  ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request){
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}
