package com.example.TikTok.service;

import com.example.TikTok.dto.request.AddComment;
import com.example.TikTok.dto.response.CommentResponse;
import com.example.TikTok.entity.Comment;
import com.example.TikTok.entity.CommentLike;
import com.example.TikTok.entity.User;
import com.example.TikTok.entity.Video;
import com.example.TikTok.enums.NotificationType;
import com.example.TikTok.mapper.CommentMapper;
import com.example.TikTok.repository.CommentRepository;
import com.example.TikTok.repository.LikeCommentRepository;
import com.example.TikTok.repository.UserRepository;
import com.example.TikTok.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final LikeCommentRepository likeCommentRepository;
    private final CommentMapper commentMapper;
    private final NotificationService notificationService;
    //ĐỆ QUY Lấy bình luận con và Check Like
    private CommentResponse mapToResponseWithReplies(Comment comment, User currentUser){
        // Dùng Mapper chuyển đổi thông tin cơ bản
        CommentResponse response = commentMapper.toResponse(comment);

        // Kiểm tra xem User hiện tại đã Like bình luận này chưa
        if(currentUser!=null){
            boolean isLiked =likeCommentRepository.existsByUserAndComment(currentUser,comment);
            response.setLiked(isLiked);
        }else{
            response.setLiked(false);
        }
        // 3. Xử lý danh sách Trả lời
        List<CommentResponse> replyResponse=new ArrayList<>();
        // Nếu comment này có câu trả lời bên trong
        if(comment.getReplies()!= null && !comment.getReplies().isEmpty()){
            for(Comment reply: comment.getReplies()){
                replyResponse.add(mapToResponseWithReplies(reply,currentUser));
            }
        }
        response.setReplies(replyResponse);
        return  response;
    }

    //LẤY DANH SÁCH BÌNH LUẬN GỐC
    public List<CommentResponse> getCommentByVideoId(Long VideoId, int page, int size){
        // Kiểm tra video có tồn tại không
        Video video = videoRepository.findById(VideoId)
                .orElseThrow(() -> new RuntimeException("Video không tồn tại"));

        // Lấy thông tin User đang đăng nhập
        String currentUserName= SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser=null;
        if(!currentUserName.equals("anonymousUser")){
            currentUser=userRepository.findByUsername(currentUserName).orElse(null);
        }
        // Phân trang: Lấy tối đa size

        Pageable pageable= PageRequest.of(page,size);
        Page<Comment> rootCommentPage=commentRepository.findByVideoAndParentIsNullOrderByCreatedAtDesc(video, pageable);


        List<CommentResponse> responses=new ArrayList<>();
        for(Comment rootComment: rootCommentPage.getContent()){
            // Gọi hàm đệ quy để xử lý comment gốc này VÀ tất cả các comment con của nó
            responses.add(mapToResponseWithReplies(rootComment,currentUser));
        }
        return responses;
    }
    @Transactional
    public CommentResponse addComment(AddComment request, Long videoId){
        String currentUserName=SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser=userRepository.findByUsername(currentUserName).orElseThrow(()->new RuntimeException("Không tìm thấy người dùng" ));
        Video video=videoRepository.findById(videoId).orElseThrow(()->new RuntimeException("Video không tồn tại"));
        Comment newcmt=commentMapper.toEntity(request);
        newcmt.setUser(currentUser);
        newcmt.setVideo(video);
        video.setCommentCount(video.getCommentCount()+1);
        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Bình luận gốc không tồn tại"));
            newcmt.setParent(parent);
        }
        commentRepository.save(newcmt);
        videoRepository.save(video);
        if (parent != null) {
            notificationService.createNotification(
                    currentUser,
                    parent.getUser(),
                    NotificationType.REPLY_COMMENT,
                    newcmt,
                    video
            );
        } else {
            notificationService.createNotification(
                    currentUser,
                    video.getUser(),
                    NotificationType.COMMENT_VIDEO,
                    newcmt,
                    video
            );
        }
        CommentResponse response = commentMapper.toResponse(newcmt);
        return response;
    }
    @Transactional
    public void toggleLikeComment(Long commentId){
        String currentUserName=SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser=userRepository.findByUsername(currentUserName).orElseThrow(()-> new RuntimeException("User không tồn tại"));
        Comment cmt=commentRepository.findById(commentId).orElseThrow(()-> new RuntimeException("Lỗi comment không tồn tại"));
        Optional<CommentLike> cmtLike=likeCommentRepository.findByUserAndComment(currentUser,cmt);
        if (cmtLike.isPresent()){
            likeCommentRepository.delete(cmtLike.get());
            if (cmt.getLikeCount()>0){
                cmt.setLikeCount(cmt.getLikeCount()-1);
            }else{
                cmt.setLikeCount(0L);
            }
            notificationService.deleteNoti(currentUser,NotificationType.COMMENT_LIKE,cmt);
        }else{
            CommentLike newCmtLike=CommentLike.builder().comment(cmt).user(currentUser).createdAt(LocalDateTime.now()).build();
            likeCommentRepository.save(newCmtLike);
            cmt.setLikeCount(cmt.getLikeCount()+1);
            notificationService.createNotification(currentUser,cmt.getUser(), NotificationType.COMMENT_LIKE,cmt,cmt.getVideo());
        }
        commentRepository.save(cmt);
    }
    @Transactional
    public Long deleteComment(Long commentId){
        String userName=SecurityContextHolder.getContext().getAuthentication().getName();
        Comment cmt= commentRepository.findById(commentId).orElseThrow(()-> new RuntimeException("Comment này không còn tồn tại"));
        Video v=videoRepository.findById(cmt.getVideo().getId()).orElseThrow(()-> new RuntimeException("Lỗi video không còn tồn tại"));
        if(!userName.equals(cmt.getUser().getUsername())&& !userName.equals(v.getUser().getUsername())){
            throw  new RuntimeException("Cmt của ta ai cho xóa ba");
        }


        long totalDeleted=countCmtToDelete(cmt);
        Long newCount=Math.max(0,v.getCommentCount()-totalDeleted);
        v.setCommentCount(newCount);
        commentRepository.delete(cmt);
        videoRepository.save(v);
        return newCount;

    }

    private long countCmtToDelete(Comment cmt){
        long count=1;
        if(cmt.getReplies()!=null&&!cmt.getReplies().isEmpty()){
            for(Comment rep : cmt.getReplies()){
                count+=countCmtToDelete(rep);
            }
        }
        return count;
    }
}
