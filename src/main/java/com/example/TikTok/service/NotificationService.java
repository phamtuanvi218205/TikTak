package com.example.TikTok.service;

import com.example.TikTok.entity.Comment;
import com.example.TikTok.entity.Notification;
import com.example.TikTok.entity.User;
import com.example.TikTok.entity.Video;
import com.example.TikTok.enums.NotificationType;
import com.example.TikTok.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    public  void createNotification(User sender, User recipient, NotificationType notificationType, Comment cmt, Video video){
        if (sender.getId()== recipient.getId()){
            return;
        }
        Notification newNoti= Notification.builder().sender(sender).recipient(recipient).comment(cmt).video(video).notificationType(notificationType).build();
        notificationRepository.save(newNoti);
    }
    public Page<Notification> getUserNotifications(User recipient, int page, int size){
        Pageable pageable= PageRequest.of(page,size, Sort.by("createdAt").descending());
        Page<Notification> lstNotification=notificationRepository.findAllByRecipient(recipient,pageable);
        return lstNotification;
    }
    public Long getUnreadCount(User recipient){
        return notificationRepository.countByRecipientAndReadFalse(recipient);
    }
    public void markAsRead(Long notificationId){
        Notification noti = notificationRepository.findById(notificationId).orElse(null);
        if (noti != null) {
            noti.setRead(true);
            notificationRepository.save(noti);
        }
    }
    @Transactional
    public void deleteNoti(User sender, NotificationType notificationType,Comment comment){
        notificationRepository.deleteBySenderAndNotificationTypeAndComment(sender, notificationType,comment);
    }
    @Transactional
    public   void deleteNotiFollow(User sender,NotificationType notificationType,User recipient){
        notificationRepository.deleteBySenderAndNotificationTypeAndRecipient(sender,notificationType,recipient);
    }
}
