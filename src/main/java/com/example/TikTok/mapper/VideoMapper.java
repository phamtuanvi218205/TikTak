package com.example.TikTok.mapper;

import com.example.TikTok.dto.response.VideoResponse;
import com.example.TikTok.entity.Video;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VideoMapper {
    @Mapping(target = "thumbnailUrl", expression = "java(video.getThumbnailUrl() != null ? video.getThumbnailUrl() : \"https://via.placeholder.com/150\")")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "fullname",source = "user.fullname")
    @Mapping(target = "userAvatar",source = "user.avatar")
    VideoResponse toResponse (Video video);
    List<VideoResponse> lstResponse(List<Video> video);
}
