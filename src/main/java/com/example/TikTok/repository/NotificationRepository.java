package com.example.TikTok.repository;

import com.example.TikTok.entity.Comment;
import com.example.TikTok.entity.Notification;
import com.example.TikTok.entity.User;
import com.example.TikTok.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    Page<Notification> findAllByRecipient(User recipient, Pageable pageable);
    Long countByRecipientAndReadFalse(User recipient);
    void deleteBySenderAndNotificationTypeAndComment(User sender, NotificationType notificationType, Comment comment);
    void deleteBySenderAndNotificationTypeAndRecipient(User sender, NotificationType notificationType, User recipient);
}
