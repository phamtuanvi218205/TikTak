package com.example.TikTok.mapper;

import com.example.TikTok.dto.request.AddComment;
import com.example.TikTok.dto.response.CommentResponse;
import com.example.TikTok.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "fullName",source = "user.fullname")
    @Mapping(target = "userName",source = "user.username")
    @Mapping(target = "avatar", expression = "java(comment.getUser().getAvatar() != null ? comment.getUser().getAvatar() : \"https://via.placeholder.com/150\")")
    @Mapping(target = "parentID",source = "parent.id")
    CommentResponse toResponse(Comment comment);
    @Mapping(target = "id",ignore = true)
    @Mapping(target = "video", ignore = true)
    @Mapping(target = "user",ignore = true)
    @Mapping(target = "likeCount", constant = "0L")
    @Mapping(target = "parent",ignore = true)
    @Mapping(target = "createdAt",ignore = true)
    @Mapping(target = "replies", ignore = true)
    Comment toEntity(AddComment request);
}
