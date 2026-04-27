package com.example.TikTok.repository;

import com.example.TikTok.entity.Follow;
import com.example.TikTok.entity.User;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow,Long> {
    long countByFollowing (User user);
    long countByFollower (User user);

    boolean existsByFollowerAndFollowing(User followeUser, User followingUser);
    Optional<Follow> findByFollowerAndFollowing(User follower,User following);
    List<Follow> findByFollower(User follower);
    Page<Follow> findByFollowing(User following, Pageable pageable);

}
