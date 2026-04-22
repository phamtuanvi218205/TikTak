package com.example.TikTok.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="Videos")
public class Video{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private  String title;
    @Column(name = "video_url", nullable = false)
    private String videoUrl;
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    private Integer duration;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "like_count")
    private Long likeCount = 0L;

    @Column(name = "comment_count")
    private Long commentCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}

