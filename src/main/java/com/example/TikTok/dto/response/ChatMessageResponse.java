package com.example.TikTok.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {
    private String id;
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead=false;
}
