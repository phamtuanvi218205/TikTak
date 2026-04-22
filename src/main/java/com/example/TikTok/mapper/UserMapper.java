package com.example.TikTok.mapper;

import com.example.TikTok.dto.request.RegisterRequest;
import com.example.TikTok.dto.response.ProfileResponse;
import com.example.TikTok.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel="spring")
public interface UserMapper {
    @Mapping(target = "password",ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "createdAt",ignore = true)
    @Mapping(target = "updatedAt",ignore = true)
    @Mapping(target = "avatar",ignore = true)
    User toEntityUser(RegisterRequest request);
    @Mapping(target = "followingCount", ignore = true)
    @Mapping(target = "followerCount", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "videos", ignore = true)
    @Mapping(target = "avatar", source = "avatar", defaultValue = "https://upload.wikimedia.org/wikipedia/commons/7/7c/Profile_avatar_placeholder_large.png")
    @Mapping(target = "bio", source = "bio", defaultValue = "Chưa có tiểu sử")

    ProfileResponse toProfileResponse (User user);
}
