package com.example.TikTok.repository;


import com.example.TikTok.entity.ChatMessage;
import com.example.TikTok.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage,String> {
    List<ChatMessage> findBySenderAndReceiverOrSenderAndReceiver(
            String sender1, String receiver1,
            String sender2, String receiver2,
            Sort sort
    );
    Long countByReceiverAndSenderAndIsReadFalse(String receiver, String sender);
    List<ChatMessage> findBySenderAndReceiverAndIsReadFalse(String sender, String receiver);
    List<ChatMessage> findAllBySenderOrReceiverOrderByTimestampDesc(String sender,String receiver);
}
