package data.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChatRoomData {
    private Integer roomId;
    private String roomName;
    private String roomType;
    private Integer targetUserId;
    private List<ChatLogDto> messages;
}