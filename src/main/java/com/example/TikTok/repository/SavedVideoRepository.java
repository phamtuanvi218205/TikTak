package com.example.TikTok.repository;

import com.example.TikTok.entity.SavedVideo;
import com.example.TikTok.entity.User;
import com.example.TikTok.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedVideoRepository extends JpaRepository<SavedVideo,Long> {
    //tìm xem video này của user này đã lưu hay chưa để hủy nếu bấm lưu lần 2
    Optional<SavedVideo> findByUserAndVideo(User user, Video video);
    //phân trang laays list video đã iu thích của user xếp theo thời gian
    Page<SavedVideo> findByUserUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    List<SavedVideo> findAllByVideo(Video video);
}
