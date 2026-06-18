package org.example.expert.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessage {

    public enum MessageType {
        ENTER, TALK, LEAVE
    }

    private MessageType type;
    private String sender;
    private String content;
    private String time;

    public static ChatMessage enter(String sender) {
        ChatMessage message = new ChatMessage();
        message.type = MessageType.ENTER;
        message.sender = sender;
        message.content = sender + "님이 입장하셨습니다.";
        message.time = now();
        return message;
    }

    public static ChatMessage leave(String sender) {
        ChatMessage message = new ChatMessage();
        message.type = MessageType.LEAVE;
        message.sender = sender;
        message.content = sender + "님이 퇴장하셨습니다.";
        message.time = now();
        return message;
    }

    public void stampTime() {
        this.time = now();
    }

    private static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
