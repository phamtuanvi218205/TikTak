package com.example.TikTok.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProfileResponse {
    private String username;
    private String fullname;
    private String avatar;
    private String bio;

    private long followingCount;
    private long followerCount;
    private long likeCount;

    private List<VideoResponse> videos;

    private boolean isFollowed;
    private boolean isBlocked;
}


