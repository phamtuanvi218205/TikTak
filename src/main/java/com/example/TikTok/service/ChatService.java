package com.example.TikTok.service;

import com.example.TikTok.dto.request.ChatMessageRequest;
import com.example.TikTok.dto.response.ChatConversationResponse;
import com.example.TikTok.dto.response.ChatMessageResponse;
import com.example.TikTok.entity.ChatMessage;
import com.example.TikTok.entity.User;
import com.example.TikTok.mapper.ChatMessageMapper;
import com.example.TikTok.repository.ChatMessageRepository;
import com.example.TikTok.repository.UserRepository;
import com.example.TikTok.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatMessageMapper chatMessageMapper;
    private final UserRepository userRepository;
    public void processAndSendMessage(ChatMessageRequest request, String senderUsername){

        ChatMessage newchat=new ChatMessage();
        newchat.setReceiver(request.getReceiver());
        newchat.setContent(EncryptionUtil.encrypt(request.getContent()));
        newchat.setSender(senderUsername);
        newchat.setTimestamp(LocalDateTime.now());
        ChatMessage savechat=chatMessageRepository.save(newchat);
        ChatMessageResponse response=chatMessageMapper.toResponse(savechat);
        response.setContent(request.getContent());
        simpMessagingTemplate.convertAndSendToUser(request.getReceiver(),"/queue/messages",response);
    }
    private void markMessagesAsRead(String sender, String receiver){
        List<ChatMessage> unReadMessage=chatMessageRepository.findBySenderAndReceiverAndIsReadFalse(sender,receiver);
        if (!unReadMessage.isEmpty()){
            unReadMessage.forEach(msg-> msg.setRead(true));
            chatMessageRepository.saveAll(unReadMessage);
        }
    }
    public long getUnreadMessage(String sender, String receiver){
        return chatMessageRepository.countByReceiverAndSenderAndIsReadFalse(receiver,sender);
    }
    public List<ChatConversationResponse> getConversation(String currentUser){
        List<ChatMessage> allMessage=chatMessageRepository.findAllBySenderOrReceiverOrderByTimestampDesc(currentUser,currentUser);
        Map<String,ChatConversationResponse> conversationResponseMap=new LinkedHashMap<>();
        for(ChatMessage mgs: allMessage){
            String partnerName;
            if(mgs.getSender().equals(currentUser)){
                partnerName=mgs.getReceiver();
            }
            else{
                partnerName=mgs.getSender();
            }
            if(conversationResponseMap.containsKey(partnerName)==false){
                Optional<User> userOptional = userRepository.findByUsername(partnerName);
                String fullname = partnerName;
                String avatar = null;
                if (userOptional.isPresent()){
                    User user=userOptional.get();
                    fullname=user.getFullname();
                    avatar=user.getAvatar();
                }
                String decryptContent=EncryptionUtil.decrypt(mgs.getContent());
                long unreadCount=getUnreadMessage(partnerName,currentUser);
                ChatConversationResponse conv = new ChatConversationResponse();
                conv.setUsername(partnerName);
                conv.setFullname(fullname);
                conv.setAvatar(avatar);
                conv.setLastMessage(decryptContent);
                conv.setLastTimestamp(mgs.getTimestamp());
                conv.setUnreadCount(unreadCount);
                conversationResponseMap.put(partnerName, conv);
            }
        }
        List<ChatConversationResponse> result = new ArrayList<>(conversationResponseMap.values());
        return  result;
    }
    public List<ChatMessageResponse> getHistoryChat(String currentUser, String partnerUser){
        markMessagesAsRead(partnerUser,currentUser);
        List<ChatMessage> chat = chatMessageRepository.findBySenderAndReceiverOrSenderAndReceiver(
                currentUser, partnerUser,
                partnerUser, currentUser,
                Sort.by(Sort.Direction.ASC, "timestamp")
        );
        List<ChatMessageResponse> responses=new ArrayList<>();
        for (ChatMessage msg: chat){
            ChatMessageResponse response =chatMessageMapper.toResponse(msg);
            response.setContent(EncryptionUtil.decrypt(msg.getContent()));
            responses.add(response);
        }
        return  responses;
    }
}
