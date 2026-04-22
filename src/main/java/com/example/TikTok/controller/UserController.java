package com.example.TikTok.controller;

import com.example.TikTok.dto.request.UpdateBioRequest;
import com.example.TikTok.dto.response.BlockResponse;
import com.example.TikTok.dto.response.ProfileResponse;
import com.example.TikTok.dto.response.UploadAvatarResponse;
import com.example.TikTok.dto.response.VideoResponse;
import com.example.TikTok.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(){
        return ResponseEntity.ok(userService.getProfile());
    }
    @PostMapping("/avatar")
    public ResponseEntity<UploadAvatarResponse> uploadAvatar(@RequestParam("file") MultipartFile file){
        UploadAvatarResponse response=userService.uploadAvatar(file);
        return  ResponseEntity.ok(response);
    }
    @GetMapping("/{username}")
    public  ResponseEntity<ProfileResponse> getUserProfile(@PathVariable String username){
        return ResponseEntity.ok(userService.getUserProfile(username));
    }
    @PostMapping("/{username}/follow")
    public  ResponseEntity<Map<String,Object>> toggleFollow(@PathVariable String username){
        return ResponseEntity.ok(userService.toggleFollow(username));
    }
    @PostMapping("/{username}/block")
    public   ResponseEntity<BlockResponse> block(@PathVariable String username){
        return ResponseEntity.ok(userService.block(username));
    }
    @GetMapping("/following")
    public ResponseEntity<?> getFollowingList() {
        return ResponseEntity.ok(userService.getFollowingList());
    }
    @PutMapping("/profile/bio")
    public  ResponseEntity<ProfileResponse> updateBio(@RequestBody UpdateBioRequest request){
        return ResponseEntity.ok(userService.updateBio(request));
    }
    @GetMapping("/{username}/profile/liked")
    public  ResponseEntity<List<VideoResponse>> getLiked(@PathVariable String username, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "15") int size){
        return ResponseEntity.ok(userService.getLikedVideo(username,page,size));
    }
    @GetMapping("/{username}/profile/saved")
    public ResponseEntity<List<VideoResponse>> getSaved(@PathVariable String username, @RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "15") int size){
        return ResponseEntity.ok(userService.getSavedVideo(username,page,size));
    }
}
