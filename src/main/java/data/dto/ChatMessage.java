package data.dto;

import lombok.Data;

@Data
public class ChatMessage {
    private Integer senderId;
    private String senderName;
    private String content;
    private String time;
    private Integer roomId;
    private String roomType;
    private Integer targetUserId;
    private boolean isMyMessage;
}