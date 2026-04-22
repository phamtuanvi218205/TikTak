package com.example.TikTok.entity;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
public class ChatMessage {
    @Id
    private String id;
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead=false;
}
