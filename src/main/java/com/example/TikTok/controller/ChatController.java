package com.example.TikTok.controller;

import com.example.TikTok.dto.request.ChatMessageRequest;
import com.example.TikTok.dto.response.ChatConversationResponse;
import com.example.TikTok.dto.response.ChatMessageResponse;
import com.example.TikTok.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal; // Import cái này
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageRequest request, Principal principal){

        chatService.processAndSendMessage(request, principal.getName());
    }

    @GetMapping("/api/chat/history/{user1}/{user2}")
    public ResponseEntity<List<ChatMessageResponse>> getHistory(@PathVariable String user1, @PathVariable String user2){
        List<ChatMessageResponse> history = chatService.getHistoryChat(user1, user2);
        return ResponseEntity.ok(history);
    }
    @GetMapping("/api/chat/conversations")
    public ResponseEntity<List<ChatConversationResponse>>   getConversations(Principal principal){
        List<ChatConversationResponse> conversationResponses=chatService.getConversation(principal.getName());
        return ResponseEntity.ok(conversationResponses);
    }
}