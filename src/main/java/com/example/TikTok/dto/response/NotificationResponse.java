package com.example.TikTok.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String type;
    private boolean read;
    private LocalDateTime createdAt;


    private String senderUsername;
    private String senderFullName;
    private String senderAvatar;


    private Long videoId;
    private Long commentId;
}
