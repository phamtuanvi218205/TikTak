package com.example.TikTok.repository;

import com.example.TikTok.entity.Comment;
import com.example.TikTok.entity.CommentLike;
import com.example.TikTok.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeCommentRepository extends JpaRepository<CommentLike,Long> {
    boolean existsByUserAndComment(User user, Comment comment);
    Optional<CommentLike> findByUserAndComment(User user,Comment comment);

}
