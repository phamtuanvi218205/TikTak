package com.example.TikTok.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GlobalSearchResponse {
    private List<SearchResponse> matchedUsers;
    private List<VideoResponse> matchedVideos;
}
