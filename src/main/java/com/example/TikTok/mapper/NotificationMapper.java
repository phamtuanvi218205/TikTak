package com.example.TikTok.mapper;

import com.example.TikTok.dto.response.NotificationResponse;
import com.example.TikTok.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(target = "type", source = "notificationType")
    @Mapping(target = "senderUsername", source = "sender.username")
    @Mapping(target = "senderFullName", source = "sender.fullname")
    @Mapping(target = "senderAvatar", source = "sender.avatar")
    @Mapping(target = "videoId", source = "video.id")
    @Mapping(target = "commentId", source = "comment.id")
    @Mapping(target = "commentContent", source = "comment.content")
    @Mapping(target = "usernameOwnerVideo", source = "video.user.username")
    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> lstResponse(List<Notification> notifications);
}
