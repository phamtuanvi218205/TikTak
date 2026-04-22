package com.example.TikTok.dto.request;


import lombok.Data;

@Data

public class AddComment {
    private String content;
    private Long parentId;
}
