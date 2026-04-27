package com.example.TikTok.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowerResponse {
    private long id;
    private String username;
    private String fullname;
    @JsonProperty("isFollowedByMe")
    private boolean isFollowedByMe;
    private String avatar;

}
