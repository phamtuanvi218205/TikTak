package com.example.TikTok.dto.request;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private String receiver;
    private String content;

}
