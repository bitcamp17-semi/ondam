package data.dto;

import lombok.Data;

@Data
public class ChatMessage {
    private String sender;
    private String content;
    private String time;
    private int groupId; // 특정 채팅 그룹 식별
}
