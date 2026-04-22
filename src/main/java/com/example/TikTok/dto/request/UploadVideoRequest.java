package com.example.TikTok.dto.request;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class UploadVideoRequest {
    private String title;
    private MultipartFile file;
}
