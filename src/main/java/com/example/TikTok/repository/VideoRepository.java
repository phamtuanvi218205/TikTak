package com.example.TikTok.repository;

import com.example.TikTok.entity.User;
import com.example.TikTok.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video,Long> {
    List<Video> findAllByUserOrderByCreatedAtDesc(User user);
    @Query("SELECT COALESCE(SUM(v.likeCount), 0) FROM Video v WHERE v.user = :user")
    Long sumLikeUser (@Param("user") User user);
    @Query("select v.id from Video v ")
    List<Long> findAllVideoByIds( );
    @Modifying
    @Query("update Video v set v.viewCount=v.viewCount+1 where v.id= :videoId")
    void updateView(@Param("videoId") Long videoId);

    @Query("select v.id from Video v where v.user.id not in(select b.blocker.id  from Block b where b.blocked.id = :userId) and v.user.id not in (select b.blocked.id from Block b where b.blocker.id = :userId )")
    List<Long> findVideoIdsExcludingBlocked(@Param("userId") Long userId);

    List<Video> findTop15ByUserOrderByLikeCountDesc(User user);
    List<Video> findByTitleContainingIgnoreCase(String title);

    @Query("select v.id from Video v where v.user.username = :username")
    List<Long> findVideoIdsByUsername(@Param("username") String username);


}
