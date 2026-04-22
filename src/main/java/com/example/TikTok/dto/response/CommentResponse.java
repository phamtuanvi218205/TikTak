package com.example.TikTok.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class CommentResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private String userName;
    private String fullName;
    private String avatar;
    private Long likeCount;
    private boolean isLiked;
    private Long parentID;
    private List<CommentResponse> replies;
}

