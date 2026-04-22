package com.example.TikTok.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    @Override
    protected void  doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain
    ) throws ServletException, IOException{
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        String path = request.getServletPath();
        // CHỈ BỎ QUA CHO WEBSOCKET. Các API /api/chat bắt buộc phải kiểm tra Token
        if (path.startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }
        final  String authHeader =request.getHeader("Authorization");
        final  String jwt;
        final String username;
        // 1. Kiểm tra Header có chứa Bearer Token không
        if(authHeader==null||!authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        // 2. Lấy Token ra (bỏ chữ "Bearer " ở đầu)
        jwt=authHeader.substring(7);
        // 3. Trích xuất Username từ Token
        try{
            username=jwtUtils.extractUsername(jwt);
        } catch (io.jsonwebtoken.ExpiredJwtException e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token expired\"}");
            return;
        }catch (Exception e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid token\"}");
            return;
        }

        // 4. Nếu có username và chưa được xác thực
        if(username!=null&& SecurityContextHolder.getContext().getAuthentication()==null){
            // Lấy thông tin User từ Database lên
            UserDetails userDetails=this.userDetailsService.loadUserByUsername(username);
            // 5. Kiểm tra Token có hợp lệ với User này không
            if(jwtUtils.isTokenValid(jwt,userDetails)){
                // Tạo đối tượng xác thực (AuthenticationToken)
                UsernamePasswordAuthenticationToken authToken=new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // 6. LƯU VÀO CONTEXT (Đánh dấu là "Đã đăng nhập")
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request,response);
    }
}
