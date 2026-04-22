package com.example.TikTok.service;

import com.example.TikTok.dto.response.GlobalSearchResponse;
import com.example.TikTok.dto.response.SearchResponse;
import com.example.TikTok.dto.response.VideoResponse;
import com.example.TikTok.entity.User;
import com.example.TikTok.entity.Video;
import com.example.TikTok.mapper.VideoMapper;
import com.example.TikTok.repository.BlockRepository;
import com.example.TikTok.repository.FollowRepository;
import com.example.TikTok.repository.UserRepository;
import com.example.TikTok.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final FollowRepository followRepository;
    private final VideoMapper videoMapper;
    public GlobalSearchResponse search(String keyword){
        String currentUsername= SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = null;

        if (!currentUsername.equals("anonymousUser")) {
            currentUser = userRepository.findByUsername(currentUsername).orElse(null);
        }
        List<User> potentialUsers = userRepository.searchByKeyword(keyword);
        JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();
        String lowerKeyword = keyword.toLowerCase();
        List<User> matchedUsers = potentialUsers.stream()
                .filter(u -> {

                    double userSim = jaroWinkler.apply(u.getUsername().toLowerCase(), lowerKeyword);


                    double fullSim = (u.getFullname() != null)
                            ? jaroWinkler.apply(u.getFullname().toLowerCase(), lowerKeyword)
                            : 0.0;


                    return Math.max(userSim, fullSim) >= 0.8;
                })

                .sorted((u1, u2) -> {
                    double sim1 = Math.max(
                            jaroWinkler.apply(u1.getUsername().toLowerCase(), lowerKeyword),
                            (u1.getFullname() != null ? jaroWinkler.apply(u1.getFullname().toLowerCase(), lowerKeyword) : 0)
                    );
                    double sim2 = Math.max(
                            jaroWinkler.apply(u2.getUsername().toLowerCase(), lowerKeyword),
                            (u2.getFullname() != null ? jaroWinkler.apply(u2.getFullname().toLowerCase(), lowerKeyword) : 0)
                    );
                    return Double.compare(sim2, sim1);
                })
                .collect(Collectors.toList());
        List<SearchResponse> userResponse=new ArrayList<>();
        for (User targetUser: matchedUsers){
            boolean isFollowed = false;
            boolean isBlocked = false;
            if (currentUser!=null){
                isFollowed=followRepository.existsByFollowerAndFollowing(currentUser,targetUser);
                boolean blockedByMe = blockRepository.existsByBlockerAndBlocked(currentUser, targetUser);
                boolean blockedByThem = blockRepository.existsByBlockerAndBlocked(targetUser, currentUser);
                isBlocked = blockedByMe || blockedByThem;
            }
            List<VideoResponse> videoResponses=new ArrayList<>();
            if (!isBlocked){
                List<Video> videos=videoRepository.findTop15ByUserOrderByLikeCountDesc(targetUser);
                videoResponses=videoMapper.lstResponse(videos);
            }
            userResponse.add(SearchResponse.builder()
                    .username(targetUser.getUsername())
                    .fullname(targetUser.getFullname())
                    .avatar(targetUser.getAvatar())
                    .isFollowed(isFollowed)
                    .isBlocked(isBlocked)
                    .videos(videoResponses)
                    .build());
        }
        List<Video> matchedVideosList = videoRepository.findByTitleContainingIgnoreCase(keyword);
        if (currentUser!=null){
            final User finalCurrentUser = currentUser;
            matchedVideosList = matchedVideosList.stream()
                    .filter(v -> !blockRepository.existsByBlockerAndBlocked(finalCurrentUser, v.getUser()) &&
                            !blockRepository.existsByBlockerAndBlocked(v.getUser(), finalCurrentUser))
                    .collect(Collectors.toList());
        }
        List<VideoResponse> videoResponses = videoMapper.lstResponse(matchedVideosList);
        return GlobalSearchResponse.builder()
                .matchedUsers(userResponse)
                .matchedVideos(videoResponses)
                .build();
    }
}
