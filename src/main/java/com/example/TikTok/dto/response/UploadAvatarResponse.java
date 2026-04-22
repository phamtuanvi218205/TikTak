package com.example.TikTok.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UploadAvatarResponse {
    private String avatarUrl;
}
