package com.example.TikTok.service;
import com.cloudinary.Cloudinary;
import com.example.TikTok.dto.request.UploadVideoRequest;
import com.example.TikTok.dto.response.VideoResponse;
import com.example.TikTok.entity.Like;
import com.example.TikTok.entity.User;
import com.example.TikTok.entity.Video;
import com.example.TikTok.enums.NotificationType;
import com.example.TikTok.mapper.VideoMapper;
import com.example.TikTok.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.Encoder;

import ws.schild.jave.encode.EncodingAttributes;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.info.VideoSize;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final VideoMapper videoMapper;
    private final LikeReopository likeReopository;
    private final CommentRepository commentRepository;
    private final FollowRepository followRepository;
    private final Cloudinary cloudinary;
    private final CloudinaryService cloudinaryService;
    private final NotificationService notificationService;
    @Value("${app.upload.dir}")
    private String uploadDir;

    //nén video xuống 720
    private void compressVideo(File source, File target) throws Exception{
        MultimediaObject object=new MultimediaObject(source);
        MultimediaInfo info=object.getInfo();
        // Cấu hình Âm thanh
        AudioAttributes audio=new AudioAttributes();
        audio.setCodec("aac");
        audio.setBitRate(128000); // 128kbps
        audio.setChannels(2);
        audio.setSamplingRate(44100);
        // Cấu hình Hình ảnh
        VideoAttributes video=new VideoAttributes();
        video.setCodec("h264");
        video.setBitRate(1500000);   // 1.5 Mbps (Chuẩn HD 720p)
        video.setFrameRate(30);      // 30 FPS

        VideoSize originalSize=info.getVideo().getSize();
        int width = originalSize.getWidth();
        int height = originalSize.getHeight();
        int newWidth = width;
        int newHeight = height;
        // video doc
        if(width>720||height>720){

            if(width<=height){
                newWidth=720;
                newHeight=(height*720)/width;
            }
            //video ngang
            else {
                newHeight=720;
                newWidth=(width*720)/height;
            }
            // Kích thước phải là số chẵn chia hết cho 16 chống rách khi đưa lên andorid
//            if (newWidth % 2 != 0) newWidth++;
//            if (newHeight % 2 != 0) newHeight++;

        }
        newWidth = (newWidth / 16) * 16;
        newHeight = (newHeight / 16) * 16;
        if (newWidth == 0) newWidth = 16;
        if (newHeight == 0) newHeight = 16;
        video.setSize(new VideoSize(newWidth,newHeight));
        EncodingAttributes attributes=new EncodingAttributes();
        attributes.setOutputFormat("mp4");
        attributes.setAudioAttributes(audio);
        attributes.setVideoAttributes(video);
        // Bắt đầu nén
        Encoder encoder=new Encoder();
        encoder.encode(object,target,attributes);
    }
    private void extractThumbnail(File sourceVideo, File targetImage) {
        try{
            MultimediaObject object =new MultimediaObject(sourceVideo);
            // Cấu hình video (chỉ lấy hình ảnh)
            VideoAttributes video=new VideoAttributes();
            video.setCodec("png");// Lưu ảnh PNG
            EncodingAttributes attrs=new EncodingAttributes();
            attrs.setVideoAttributes(video);
            attrs.setOutputFormat("image2");// Format đặc biệt để xuất ảnh
            attrs.setOffset(1f);// Cắt tại giây thứ 1
            attrs.setDuration(0.01f);
            Encoder encoder=new Encoder();
            encoder.encode(object,targetImage,attrs);
        }catch (Exception e) {
            System.err.println("Lỗi cắt thumbnail: " + e.getMessage());
            // Không throw exception để luồng upload video vẫn tiếp tục dù lỗi ảnh
        }


    }
    //  HÀM UPLOAD CHÍNH
    public VideoResponse uploadVideo(UploadVideoRequest request) throws IOException {
        String currentUsername= SecurityContextHolder.getContext().getAuthentication().getName();
        User user= userRepository.findByUsername(currentUsername).orElseThrow(()->new RuntimeException("Lỗi người dùng không tồn tại"));
        MultipartFile file=request.getFile();
        if (file.isEmpty()) throw new RuntimeException("Chưa tải video lên");
        // Tạo thư mục nếu chưa có
        String projectDir = System.getProperty("user.dir");
        Path rootPath = Paths.get(projectDir, uploadDir, "video");
        if (!Files.exists(rootPath)) {
            Files.createDirectories(rootPath);
        }
        Path imagePath=Paths.get(projectDir,uploadDir,"images");
        if (!Files.exists(imagePath)){
            Files.createDirectories(imagePath);
        }
        String baseName= UUID.randomUUID().toString();
        String originalExt = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        // File TẠM  -> lưu vào uploads/video/
        File sourceFile=new File(rootPath.toFile(),baseName+"_raw"+originalExt);
        // File ĐÍCH  -> lưu vào uploads/video/
        File targetFile=new File(rootPath.toFile(),baseName+".mp4");
        File thumbnailFile=new File(imagePath.toFile(),baseName+".png");
        String videoCloudUrl="";
        String thumbCloudUrl="https://via.placeholder.com/300x500.png?text=Thumbnail";
        try{
            // Lưu file gốc xuống ổ cứng
            file.transferTo(sourceFile);
            compressVideo(sourceFile,targetFile);
            extractThumbnail(sourceFile,thumbnailFile);
            // Đẩy file ĐÃ NÉN và ẢNH lên Cloudinary
            videoCloudUrl=cloudinaryService.uploadFile(targetFile);
            if (thumbnailFile.exists()){
                thumbCloudUrl = cloudinaryService.uploadFile(thumbnailFile);
            }
            if (sourceFile.exists()) sourceFile.delete();
            if (targetFile.exists()) targetFile.delete();
            if (thumbnailFile.exists()) thumbnailFile.delete();
        }catch (Exception e){
            // Nếu lỗi thì dọn dẹp cả 2 file
            if (sourceFile.exists()) sourceFile.delete();
            if (targetFile.exists()) targetFile.delete();
            if (thumbnailFile.exists()) thumbnailFile.delete();
            throw new RuntimeException("Lỗi xử lý video: " + e.getMessage());
        }
        // Lưu vào Database

        Video video=Video.builder().title(request.getTitle()).videoUrl(videoCloudUrl).thumbnailUrl(thumbCloudUrl).user(user)
                .viewCount(0L).likeCount(0L).commentCount(0L).createdAt(LocalDateTime.now()).build();
        videoRepository.save(video);
        return videoMapper.toResponse(video);
    }
    // Key để lưu trữ trong Session
    private static final String SESSION_KEY="TIKTOK_VIDEO_IDS";
    public List<VideoResponse> getVideo(int page, int size, HttpSession session, boolean reset, Long focusId, String focusUser){
        String currentUsername=SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser=null;
        if (!currentUsername.equals("anonymousUser")){
            currentUser=userRepository.findByUsername(currentUsername).orElseThrow(()->new RuntimeException("Chưa đăng nhập"));

        }
        // Lấy danh sách ID video từ Session của người dùng
        List<Long> playListID= (List<Long>) session.getAttribute(SESSION_KEY);
        // Nếu chưa có (người mới) hoặc Frontend yêu cầu reset (đã xem hết) -> Tạo list mới
        if (playListID==null||playListID.isEmpty()||reset){
            if (currentUser!=null){
                playListID=videoRepository.findVideoIdsExcludingBlocked(currentUser.getId());
            }
            else {
                playListID=videoRepository.findAllVideoByIds();// Lấy tất cả ID video từ DB
            }

            Collections.shuffle(playListID);// Xáo trộn ngẫu nhiên
             //lưu vào session
            if (focusId!=null&& focusUser!=null){
                List<Long> userVideoIds=videoRepository.findVideoIdsByUsername(focusUser);
                playListID.removeAll(userVideoIds);
                playListID.remove(focusId);
                List<Long> custome=new ArrayList<>();
                custome.add(focusId);
                userVideoIds.remove(focusId);
                Collections.shuffle(userVideoIds);
                custome.addAll(userVideoIds);
                custome.addAll(playListID);
                playListID = custome;
            }
            session.setAttribute(SESSION_KEY,playListID);
        }
        // Tính toán vị trí cắt danh sách
        int start=page*size;

        int end = Math.min(start + size, playListID.size());
        // Nếu trang yêu cầu vượt quá số lượng video -> Trả về rỗng
        if (start >= playListID.size()) {
            return new ArrayList<>();
        }
        // Lấy các ID của trang hiện tại
        List<Long> pageID=playListID.subList(start,end);
        // Query lấy thông tin chi tiết Video từ DB theo list ID này
        List<Video> videos=videoRepository.findAllById(pageID);
        // SẮP XẾP LẠI: findAllById không trả về đúng thứ tự ID truyền vào
        // map lại để đảm bảo thứ tự ngẫu nhiên đã lưu trong session
        Map<Long,Video> videoMap=videos.stream().collect(Collectors.toMap(Video::getId, Function.identity()));
        List<Video> sortedVideo= new ArrayList<>();
        for(Long id: pageID){
            if (videoMap.containsKey(id)){
                sortedVideo.add(videoMap.get(id));
            }
        }

        List<VideoResponse> responses=new ArrayList<>();
        for (Video v: sortedVideo){
            VideoResponse res=videoMapper.toResponse(v);
            res.setCmtCount(commentRepository.countByVideo(v));
            if (currentUser!=null){
                boolean isLiked=likeReopository.existsByUserAndVideo(currentUser,v);
                boolean isFollowed=followRepository.existsByFollowerAndFollowing(currentUser,v.getUser());
                res.setIsFollowed(isFollowed);
                res.setLiked(isLiked);
            }else{
                res.setLiked(false);
                res.setIsFollowed(false);
            }
            responses.add(res);
        }
        return responses;
    }
    public VideoResponse toggleLike(Long videoId){
        String currentUsername=SecurityContextHolder.getContext().getAuthentication().getName();
        User user=userRepository.findByUsername(currentUsername).orElseThrow(()-> new RuntimeException("Hãy đăng nhập để thích video"));
        Video video=videoRepository.findById(videoId).orElseThrow(()->new RuntimeException("Video không còn tồn tại"));
        Optional<Like> likedVideo=likeReopository.findByUserAndVideo(user,video);
        if(likedVideo.isPresent()) {
            likeReopository.delete(likedVideo.get());
            video.setLikeCount(video.getLikeCount() - 1);
        }else{
            Like newLiked= Like.builder().user(user).video(video).createdAt(LocalDateTime.now()).build();
            likeReopository.save(newLiked);
            video.setLikeCount(video.getLikeCount()+1);
            notificationService.createNotification(user,video.getUser(), NotificationType.LIKE_VIDEO,null,video);
        }
        videoRepository.save(video);
        VideoResponse response=videoMapper.toResponse(video);
        response.setLiked(likedVideo.isEmpty());
        return response;
    }
    @Transactional
    public void updateView(Long videoId){
        String curentusername=SecurityContextHolder.getContext().getAuthentication().getName();

        Video v=videoRepository.findById(videoId).orElse(null);
        if (v==null){
            return;
        }
        if (v.getUser().getUsername().equals(curentusername))
        {
            return;
        }
        videoRepository.updateView(videoId);
    }
}
