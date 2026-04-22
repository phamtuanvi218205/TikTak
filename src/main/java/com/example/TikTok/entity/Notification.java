package com.example.TikTok.entity;

import com.example.TikTok.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Date;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "recipient_id",nullable = false)
    private User recipient;
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    //loai thong bao
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;
    @Column(name="is_read",nullable = false)
    private boolean read = false;
    @ManyToOne()
    @JoinColumn(name = "video_id")
    private Video video;
    @ManyToOne()
    @JoinColumn(name="comment_id")
    private Comment comment;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
