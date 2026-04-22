package com.example.TikTok.service;

import com.example.TikTok.dto.request.UpdateBioRequest;
import com.example.TikTok.dto.response.BlockResponse;
import com.example.TikTok.dto.response.ProfileResponse;
import com.example.TikTok.dto.response.UploadAvatarResponse;
import com.example.TikTok.dto.response.VideoResponse;
import com.example.TikTok.entity.*;
import com.example.TikTok.enums.NotificationType;
import com.example.TikTok.mapper.UserMapper;
import com.example.TikTok.mapper.VideoMapper;
import com.example.TikTok.repository.*;
import lombok.RequiredArgsConstructor;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final VideoRepository videoRepository;
    private final FollowRepository followRepository;
    private final VideoMapper videoMapper;
    private final BlockRepository blockRepository;
    private final CloudinaryService cloudinaryService;
    private final LikeReopository likeReopository;
    private final SavedVideoRepository savedVideoRepository;
    private final NotificationService notificationService;
    @Value("${app.upload.dir}")
    private String uploadDir;

    public ProfileResponse getProfile(){
        String currentName= SecurityContextHolder.getContext().getAuthentication().getName();
        User user=userRepository.findByUsername(currentName).orElseThrow(()-> new RuntimeException("User không tồn tại"));
        List<Video> videos=videoRepository.findAllByUserOrderByCreatedAtDesc(user);
        long followers = followRepository.countByFollowing(user);
        long following = followRepository.countByFollower(user);
        long totalLikes = videoRepository.sumLikeUser(user);
        ProfileResponse profileResponse= userMapper.toProfileResponse(user);
        List<VideoResponse> lstVideo= videoMapper.lstResponse(videos);
        profileResponse.setFollowerCount(followers);
        profileResponse.setFollowingCount(following);
        profileResponse.setLikeCount(totalLikes);
        profileResponse.setVideos(lstVideo);
        return profileResponse;
    }
    public UploadAvatarResponse uploadAvatar(MultipartFile file){
        try {
            String currentUserName=SecurityContextHolder.getContext().getAuthentication().getName();
            User user=userRepository.findByUsername(currentUserName).orElseThrow(()->new RuntimeException("Lỗi user không tồn tại"));
            if (file.isEmpty()){
                throw new RuntimeException("Chưa chọn ảnh!");
            }
            String avatarCloudUrl= cloudinaryService.uploadFile(file);

            user.setAvatar(avatarCloudUrl);
            userRepository.save(user);
            return UploadAvatarResponse.builder().avatarUrl(avatarCloudUrl).build();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi lưu file: " + e.getMessage());
        }
    }
    public ProfileResponse getUserProfile(String targetUsername){
        User target=userRepository.findByUsername(targetUsername).orElseThrow(()->new RuntimeException("Người dùng không tồn tại"));
        List<Video> video=videoRepository.findAllByUserOrderByCreatedAtDesc(target);
        long follower=followRepository.countByFollowing(target);
        long following=followRepository.countByFollower(target);
        long totalLike=videoRepository.sumLikeUser(target);
        boolean isFollowed=false;
        boolean isBlocked = false;
        String currentUsername=SecurityContextHolder.getContext().getAuthentication().getName();
        if (!currentUsername.equals("anonymousUser")){
            User user=userRepository.findByUsername(currentUsername).orElse(null);
            if (user!=null) {
                isFollowed = followRepository.existsByFollowerAndFollowing(user, target);
                boolean blockedByMe = blockRepository.existsByBlockerAndBlocked(user, target);
                boolean blockedByTarget = blockRepository.existsByBlockerAndBlocked(target, user);
                if (blockedByMe || blockedByTarget) {
                    isBlocked = true;
                }
            }
        }
        ProfileResponse response=userMapper.toProfileResponse(target);
        if (isBlocked){
            response.setBlocked(true);
            response.setFollowed(false);
            response.setVideos(new ArrayList<>());
            response.setFollowerCount(0L);
            response.setFollowingCount(0L);
            response.setLikeCount(0L);
            return response;
        }
        response.setBlocked(false);
        List<VideoResponse> videoResponse=videoMapper.lstResponse(video);
        response.setFollowed(isFollowed);
        response.setLikeCount(totalLike);
        response.setFollowerCount(follower);
        response.setFollowingCount(following);
        response.setVideos(videoResponse);
        return response;
    }
    @Transactional
    public Map<String,Object> toggleFollow(String username){
        String currentUsername=SecurityContextHolder.getContext().getAuthentication().getName();
        User follower=userRepository.findByUsername(currentUsername).orElseThrow(()->new RuntimeException("Chưa đăng nhập"));
        User following=userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("Lỗi user không tồn tại"));

        if(follower.getUsername().equals(username)){
            throw new RuntimeException("Không thể tự follow chính mình");
        }else{
            boolean isFollowed;
            Optional<Follow> existFollow=followRepository.findByFollowerAndFollowing(follower,following);
            if(existFollow.isPresent()){
                followRepository.delete(existFollow.get());
                isFollowed=false;
                notificationService.deleteNotiFollow(follower,NotificationType.FOLLOW,following);
            }else{
                Follow newflw=new Follow();
                newflw.setFollowing(following);
                newflw.setFollower(follower);
                newflw.setCreatedAt(LocalDateTime.now());
                followRepository.save(newflw);
                isFollowed=true;
                notificationService.createNotification(follower,following, NotificationType.FOLLOW,null,null);
            }
            Long newFollowedCount= followRepository.countByFollowing(following);
            Map<String,Object> response=new HashMap<>();
            response.put("isFollowed",isFollowed);
            response.put("newFollowerCount",newFollowedCount);
            return response;
        }


    }
    @Transactional
    public BlockResponse block(String username){
        String curentUsername=SecurityContextHolder.getContext().getAuthentication().getName();
        User currUser=userRepository.findByUsername(curentUsername).orElseThrow(()-> new RuntimeException("Bạn chưa đăng nhập"));
        User target=userRepository.findByUsername(username).orElseThrow(()-> new RuntimeException("Người dùng này không tồn tại"));
        BlockResponse response=new BlockResponse();

            if (currUser.getUsername().equals(username)){
                throw new RuntimeException("Bạn khoong thể tự chặn chính mình");
            }

            Optional<Block> exitsBlock=blockRepository.findByBlockerAndBlocked(currUser,target);
            if (exitsBlock.isPresent()){
                blockRepository.delete(exitsBlock.get());
                response.setBlocked(false);
            }else{
                Optional<Follow> exitsFollow=followRepository.findByFollowerAndFollowing(currUser,target);
                if (exitsFollow.isPresent()){
                    followRepository.delete(exitsFollow.get());

                }
                Optional<Follow> exitsFollow2=followRepository.findByFollowerAndFollowing(target,currUser);
                if (exitsFollow2.isPresent()){
                    followRepository.delete(exitsFollow2.get());

                }
                Block newBlock=new Block();
                newBlock.setBlocked(target);
                newBlock.setBlocker(currUser);
                newBlock.setCreatedAt(LocalDateTime.now());
                blockRepository.save(newBlock);
                response.setBlocked(true);
            }

        return  response;
    }
    public List<Map<String,String>> getFollowingList(){
        String currUsername=SecurityContextHolder.getContext().getAuthentication().getName();
        User currUser=userRepository.findByUsername(currUsername).orElseThrow(()-> new RuntimeException("Chưa đăng nhập"));
        List<Follow> follows=followRepository.findByFollower(currUser);
        List<Map<String,String>> result=new ArrayList<>();
        for (Follow fl : follows){
            Map<String,String> userMap=new HashMap<>();
            userMap.put("username", fl.getFollowing().getUsername());
            userMap.put("fullname",fl.getFollowing().getFullname());
            userMap.put("avatar", fl.getFollowing().getAvatar());
            result.add(userMap);
        }
        return result;
    }
    public ProfileResponse updateBio(UpdateBioRequest request){
        if (request.getBio() != null && request.getBio().length() > 80) {
            throw new RuntimeException("Tiểu sử không được vượt quá 80 ký tự!");
        }
        String curUsername=SecurityContextHolder.getContext().getAuthentication().getName();
        User user=userRepository.findByUsername(curUsername).orElseThrow(()-> new RuntimeException("Bạn chưa dăng nhập"));
        user.setBio(request.getBio());
        userRepository.save(user);
        ProfileResponse response=userMapper.toProfileResponse(user);
        return response;

    }

    public List<VideoResponse> getLikedVideo(String username, int page, int size){
        if (!userRepository.existsByUsername(username)) {
            throw new RuntimeException("Người dùng không tồn tại");
        }
        Pageable pageable= PageRequest.of(page,size);
        Page<Like> liked = likeReopository.findByUserUsernameAndVideoUserUsernameNotOrderByCreatedAtDesc(
                username,
                username,
                pageable
        );
        return  liked.getContent().stream()
                .map(like -> {
            Video video=like.getVideo();
            VideoResponse response=videoMapper.toResponse(video);
            response.setLiked(true);
            return  response;
        }).collect(Collectors.toList());
    }
    @Transactional
    public Map<String,Object> savedVideo(Long videoId){
        String currUsername=SecurityContextHolder.getContext().getAuthentication().getName();
        User currUser=userRepository.findByUsername(currUsername).orElseThrow(()-> new RuntimeException("Bạn chưa đăng nhaapj"));
        Video video=videoRepository.findById(videoId).orElseThrow(()-> new RuntimeException("Video không tồn tại"));
        boolean isSaved;
        Optional<SavedVideo> exist=savedVideoRepository.findByUserAndVideo(currUser,video);
        if (exist.isPresent()){
            savedVideoRepository.delete(exist.get());
            isSaved=false;
        }else{
            SavedVideo newSaved=SavedVideo.builder().video(video).user(currUser).build();
            savedVideoRepository.save(newSaved);
            isSaved=true;
        }
        Map<String,Object> response=new HashMap<>();
        response.put("video_id",video);
        response.put("user_id",currUser);
        return response;
    }
    public List<VideoResponse> getSavedVideo(String username, int page, int size){
        if (!userRepository.existsByUsername(username)){
            throw new RuntimeException("Người dùng không tồn tại");
        }
        Pageable pageable=PageRequest.of(page,size);
        Page<SavedVideo> pageSaved=savedVideoRepository.findByUserUsernameOrderByCreatedAtDesc(username,pageable);
        return pageSaved.getContent().stream().map(savedVideo -> {
            Video video=savedVideo.getVideo();
            VideoResponse response=videoMapper.toResponse(video);
            response.setIsSaved(true);
            return response;
        }).collect(Collectors.toList());
    }
}
