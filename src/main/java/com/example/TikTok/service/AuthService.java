package com.example.TikTok.service;

import com.example.TikTok.dto.request.*;
import com.example.TikTok.dto.response.AuthResponse;
import com.example.TikTok.entity.PasswordResetToken;
import com.example.TikTok.entity.RefreshToken;
import com.example.TikTok.entity.User;
import com.example.TikTok.mapper.UserMapper;
import com.example.TikTok.repository.PasswordResetTokenRepository;
import com.example.TikTok.repository.RefreshTokenRepository;
import com.example.TikTok.repository.UserRepository;
import com.example.TikTok.security.JwtUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final String googleClientId = "107896090043-tnbgj32mnppi94u3cf82be7enavj37k3.apps.googleusercontent.com";
    public AuthResponse loginWithGoogle(GoogleLoginRequest request){
        GoogleIdTokenVerifier verifier= new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                // truyen client id vao de gg biet day la cua web tiktak
                .setAudience(Collections.singletonList(googleClientId))
                .build();
        try{
            GoogleIdToken idToken=verifier.verify(request.getIdToken());
            if (idToken!=null){
                GoogleIdToken.Payload payload=idToken.getPayload();
                String email=payload.getEmail();
                String name= (String) payload.get("name");
                String pictureUrl=(String) payload.get("picture");
                // tim user trong db theo email xem co khong
                User user=userRepository.findByEmail(email).orElse(null);
                if (user==null){
                    user =new User();
                    user.setEmail(email);
                    user.setFullname(name);
                    user.setAvatar(pictureUrl);
                    String prefix=email.split("@")[0];
                    // tao username theo email vaf random 4 ky tu di kem
                    user.setUsername(prefix+"_"+UUID.randomUUID().toString().substring(0,4));
                    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    user=userRepository.save(user);

                }
                // neu da co hoac vua tao xong thi cap token
                String accessToken= jwtUtils.generateToken(user);
                RefreshToken refreshToken=createRefreshToken(user);
                return AuthResponse.builder()
                        .username(user.getUsername())
                        .token(accessToken)
                        .refreshToken(refreshToken.getRefreshToken())
                        .message("Đăng nhập bằng Google thành công!").build();
            }else{
                throw new RuntimeException("Mã xác thực Google không hợp lệ hoặc đã hết hạn!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi trong quá trình kết nối với máy chủ Google: " + e.getMessage());
        }
    }
    public AuthResponse register(RegisterRequest request){
        if(userRepository.existsByUsername(request.getUsername()))
        {
            throw new RuntimeException("Username đã tồn tại!");
        }
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email đã tồn tại!");
        }
        User ur=userMapper.toEntityUser(request);
        ur.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(ur);
        String Token=jwtUtils.generateToken(ur);
        return AuthResponse.builder().token(Token).username(ur.getUsername()).message("Đăng ký thành công").build();
    }
    private RefreshToken createRefreshToken(User user){
        // Tìm xem user đã có token cũ chưa, có thì ghi đè, chưa thì tạo mới
        RefreshToken existToken=refreshTokenRepository.findByUser(user).orElse(new RefreshToken());
        existToken.setUser(user);
        existToken.setRefreshToken(UUID.randomUUID().toString());
        existToken.setExpiryDate(Instant.now().plusMillis(604800000L)); // Hết hạn sau 7 ngày (7 * 24 * 60 * 60 * 1000)
        return refreshTokenRepository.save(existToken);
    }
    public AuthResponse refreshToken(RefreshTokenRequest request){
        String RequestRefreshToken=request.getRefreshToken();
        RefreshToken tokenDB=refreshTokenRepository.findByRefreshToken(RequestRefreshToken).orElse(null);
        if(tokenDB==null){
            throw new RuntimeException("Refresh token không tồn tại trong DB!");
        }
        if(tokenDB.getExpiryDate().compareTo(Instant.now())<0 ){
            refreshTokenRepository.delete(tokenDB);
            throw new RuntimeException("Refresh token đã hết hạn. Vui lòng đăng nhập lại!");
        }
        User user=tokenDB.getUser();
        //Cấp Access Token mới tinh
        String newAccessToken= jwtUtils.generateToken(user);
        return AuthResponse.builder().token(newAccessToken).refreshToken(RequestRefreshToken).username(user.getUsername()).build();
    }
//    private RefreshToken verifyRefreshToken(RefreshToken refreshToken){
//        if(refreshToken.getExpiryDate().compareTo(Instant.now())<0){
//            refreshTokenRepository.delete(refreshToken); // Hết hạn thì xóa luôn trong DB
//            throw new RuntimeException("Refresh token đã hết hạn. Vui lòng đăng nhập lại!");
//        }
//        return refreshToken;
//    }

    public AuthResponse login(LoginRequest request){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword()));
        User user=userRepository.findByUsername(request.getUsername()).orElseThrow(()->new RuntimeException("User không tồn tại"));
        String accessToken= jwtUtils.generateToken(user);
        RefreshToken refreshToken= createRefreshToken(user);
        return AuthResponse.builder().username(user.getUsername()).token(accessToken).refreshToken(refreshToken.getRefreshToken()).message("Đăng nhập thành công").build();
    }
    public String forgotPassword(String email){
        User user=userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("Email sai "));
        String otp=String.valueOf(new Random().nextInt(900000) + 100000);
        PasswordResetToken token=passwordResetTokenRepository.findByUser(user).orElse(new PasswordResetToken());
        token.setUser(user);
        token.setOtp(otp);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        passwordResetTokenRepository.save(token);
        emailService.sendMail(email,"Mã xác thực quên mật khẩu TikTok",
                "Mã OTP của bạn là: " + otp + "\nMã này sẽ hết hạn sau 5 phút."
        );
        return "Đã gửi mã OTP về email!";
    }
    public String resetPassword(ResetPasswordRequest request){
        User user=userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException("User không tìm thấy!"));
        PasswordResetToken token= passwordResetTokenRepository.findByOtpAndUser(request.getOtp(),user).orElseThrow(() -> new RuntimeException("Mã OTP không đúng!"));
        if(token.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Mã OTP đã hết hạn!");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        passwordResetTokenRepository.delete(token);
        return "Đặt lại mật khẩu thành công!";
    }
}
