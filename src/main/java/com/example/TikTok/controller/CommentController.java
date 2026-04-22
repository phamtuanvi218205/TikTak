package com.example.TikTok.controller;

import com.example.TikTok.dto.request.AddComment;
import com.example.TikTok.dto.response.CommentResponse;
import com.example.TikTok.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;


    @GetMapping("/videos/{videoId}/comments")
    public ResponseEntity<List<CommentResponse>> getComment(
            @PathVariable Long videoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size){
        List<CommentResponse> comments = commentService.getCommentByVideoId(videoId, page, size);
        return ResponseEntity.ok(comments);
    }


    @PostMapping("/videos/{videoId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @RequestBody AddComment request,
            @PathVariable Long videoId){
        CommentResponse response = commentService.addComment(request, videoId);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> toggleLikeComment(@PathVariable Long commentId){
        commentService.toggleLikeComment(commentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comments/{commentId}/delete")
    public ResponseEntity<Long> deleteComments(@PathVariable Long commentId){
        Long cmtCount=commentService.deleteComment(commentId);
        return ResponseEntity.ok(cmtCount);
    }
}