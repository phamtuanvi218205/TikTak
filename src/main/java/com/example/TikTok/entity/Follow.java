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
@Table(name="Follows",uniqueConstraints = {@UniqueConstraint(columnNames = {"follower_id","following_id"})})

public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="following_id", nullable = false)
    private User following;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="follower_id",nullable = false)
    private User follower;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
