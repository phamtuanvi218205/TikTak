package com.example.TikTok.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResponse {
    private String username;
    private String fullname;
    private String avatar;
    private List<VideoResponse> videos;
    private boolean isFollowed=false;
    private boolean isBlocked=false;
}
