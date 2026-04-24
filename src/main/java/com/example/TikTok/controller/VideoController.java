package com.example.TikTok.controller;

import com.example.TikTok.dto.request.UploadVideoRequest;
import com.example.TikTok.dto.response.VideoResponse;
import com.example.TikTok.service.UserService;
import com.example.TikTok.service.VideoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {
    private final VideoService videoService;
    private final UserService userService;
    @PostMapping(value = "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?>uploadVideo(@ModelAttribute UploadVideoRequest request){
        try{
            VideoResponse response=videoService.uploadVideo(request);
            return ResponseEntity.ok(response);
        }catch (IOException e){
            return ResponseEntity.internalServerError().body("Lỗi ghi file: " + e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xử lý: " + e.getMessage());
        }
    }
    @GetMapping("/feed")
    public ResponseEntity<List<VideoResponse>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "false") boolean reset,
            @RequestParam(required = false) Long focusId,
            @RequestParam(required = false) String focusUser,
            HttpSession session) {


        return ResponseEntity.ok(videoService.getVideo(page, size, session, reset, focusId, focusUser));
    }
    @PostMapping("/{id}/like")
    public ResponseEntity<?>  toggleLike(@PathVariable Long id){
        try{
            VideoResponse response=videoService.toggleLike(id);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.status(401).body("Vui lòng đăng nhập để thích video");
        }
    }
    @PostMapping("/{videoId}/view")
    public ResponseEntity<?> updateView(@PathVariable Long videoId){
        videoService.updateView(videoId);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/{videoId}/save")
    public ResponseEntity<Map<String,Object>> saveVideo(@PathVariable Long videoId){
        return ResponseEntity.ok(userService.savedVideo(videoId));
    }
    @DeleteMapping("/{videoId}/delete")
    public ResponseEntity<?> deleteVideo(@PathVariable Long videoId){
        videoService.deleteVideo(videoId);
        return ResponseEntity.ok().build();
    }
}
