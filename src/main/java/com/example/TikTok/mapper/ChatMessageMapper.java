package com.example.TikTok.mapper;

import com.example.TikTok.dto.response.ChatMessageResponse;
import com.example.TikTok.entity.ChatMessage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {
    ChatMessageResponse toResponse(ChatMessage chatMessage);

}
