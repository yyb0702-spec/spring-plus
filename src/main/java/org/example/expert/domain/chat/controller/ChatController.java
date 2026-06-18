package org.example.expert.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.chat.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    // 입장: /pub/chat.enter
    @MessageMapping("/chat.enter")
    public void enter(ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        // 세션에 닉네임 저장 (퇴장 시 사용)
        headerAccessor.getSessionAttributes().put("sender", message.getSender());
        messagingTemplate.convertAndSend("/sub/chat", ChatMessage.enter(message.getSender()));
    }

    // 메시지 전송: /pub/chat.send
    @MessageMapping("/chat.send")
    public void send(ChatMessage message) {
        message.stampTime();
        messagingTemplate.convertAndSend("/sub/chat", message);
    }
}
