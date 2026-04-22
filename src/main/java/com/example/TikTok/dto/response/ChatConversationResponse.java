package com.example.TikTok.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatConversationResponse {
    private String username;
    private String fullname;
    private String avatar;
    private String lastMessage;
    private long unreadCount;
    private LocalDateTime lastTimestamp;
}
