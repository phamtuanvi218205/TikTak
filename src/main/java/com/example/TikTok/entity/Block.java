package com.example.TikTok.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "block",uniqueConstraints = {@UniqueConstraint(columnNames = {"blocker_id","blocked_id"})})
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id",nullable = false)
    private User blocker;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id",nullable = false)
    private User blocked;
    private LocalDateTime createdAt;
}
