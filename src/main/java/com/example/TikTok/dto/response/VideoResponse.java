package com.example.TikTok.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class VideoResponse {
    private long id;
    private String title;
    private String videoUrl;
    private String thumbnailUrl;
    private long viewCount;
    private long likeCount;
    private long cmtCount;
    private String username;
    private String fullname;
    private String userAvatar;
    private Boolean liked;
    private Boolean isFollowed;
    private Boolean isSaved;
}
