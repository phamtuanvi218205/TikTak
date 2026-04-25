package com.example.TikTok.controller;

import com.example.TikTok.dto.response.NotificationResponse;
import com.example.TikTok.entity.Notification;
import com.example.TikTok.entity.User;
import com.example.TikTok.mapper.NotificationMapper;
import com.example.TikTok.repository.UserRepository;
import com.example.TikTok.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;


    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vui lòng đăng nhập"));
    }


    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User currentUser = getCurrentUser();
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(currentUser, page, size);




        return ResponseEntity.ok(notifications);
    }


    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        User currentUser = getCurrentUser();
        Long count = notificationService.getUnreadCount(currentUser);

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã đánh dấu đọc");
        return ResponseEntity.ok(response);
    }
}