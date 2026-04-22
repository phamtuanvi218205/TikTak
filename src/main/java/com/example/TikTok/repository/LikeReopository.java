package com.example.TikTok.repository;

import com.example.TikTok.entity.Like;
import com.example.TikTok.entity.User;
import com.example.TikTok.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeReopository extends JpaRepository<Like,Long> {
    boolean existsByUserAndVideo(User user, Video video);
    Optional<Like> findByUserAndVideo(User user, Video video);
    Page<Like> findByUserUsernameOrderByCreatedAtDesc(String username, Pageable pageable);
    Page<Like> findByUserUsernameAndVideoUserUsernameNotOrderByCreatedAtDesc(
            String likerUsername,
            String ownerUsername,
            Pageable pageable
    );
}
