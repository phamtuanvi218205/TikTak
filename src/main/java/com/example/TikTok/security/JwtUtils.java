package com.example.TikTok.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import com.example.TikTok.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private  String secretKey;
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Hết hạn sau 1 ngày
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    public String generateToken(UserDetails userDetails){
        Map<String,Object>claims=new HashMap<>();

        if(userDetails instanceof User){
            User customUser=(User) userDetails;
            claims.put("userId",customUser.getId());
            claims.put("fullName",customUser.getFullname());
            claims.put("avatar",customUser.getAvatar());
            claims.put("email",customUser.getEmail());
            List<String> roles=customUser.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            claims.put("roles",roles);
        }
        return createToken(claims, userDetails.getUsername());
    }

    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }
    // Đây là một hàm Generic Dùng chung xài Java 8 Functional Interface.
    // Nó lấy toàn bộ cái ruột ở hàm trên, sau đó nếu muốn moi trường nào Tên, Ngày hết hạn... thì truyền hàm moi vào (claimsResolver).
    public <T> T extractClaim(String token, Function<Claims,T> claimsResolver){
        final Claims claims= extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    // Ứng dụng hàm Generic ở trên để lấy đúng cái Username (Subject)
    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }
    // Ứng dụng hàm Generic ở trên để lấy đúng Ngày hết hạn (Expiration)
    public Date extractExpiration(String token){
        return extractClaim(token,Claims::getExpiration);
    }
    // Lấy ngày hết hạn của Token so sánh xem nó có nằm trước thời điểm hiện tại hay không.
    // Nếu nằm trước thì đã hết hạn .
    private Boolean isTokenExpired (String token){
        return extractExpiration(token).before(new Date());
    }
    public boolean  isTokenValid(String token, UserDetails userDetails){
        // Moi Username từ Token ra
        final String username = extractUsername(token);
        //Username trong Thẻ PHẢI GIỐNG hệt Username của Database (userDetails.getUsername())
        //Thẻ chưa hết hạn
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
