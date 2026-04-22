package com.example.TikTok.repository;

import com.example.TikTok.entity.Block;
import com.example.TikTok.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {
    boolean existsByBlockerAndBlocked(User blocker,User Blocked);
    Optional<Block> findByBlockerAndBlocked(User blocker, User Blocked);
}
