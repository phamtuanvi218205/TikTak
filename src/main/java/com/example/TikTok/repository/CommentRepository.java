package com.example.TikTok.repository;

import com.example.TikTok.entity.Comment;
import com.example.TikTok.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    Page<Comment> findByVideoAndParentIsNullOrderByCreatedAtDesc(Video video, Pageable pageable);

    long countByVideo(Video v);
    long countByParent(Comment comment);
}

