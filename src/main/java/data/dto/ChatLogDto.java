package data.dto;

import lombok.Data;
import org.apache.ibatis.type.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;

@Data
@Alias("ChatLogDto")
public class ChatLogDto {
	private Integer id; // int -> Integer로 변경 (null 허용 및 객체 타입 통일)
    private Integer senderId; // int -> Integer로 변경
    private Integer receiverId; // int -> Integer로 변경
    @JsonProperty("isRead")
    private boolean isRead;
    private String message; // content -> message로 통일
    private Integer groupId;
    private String file;
    private Timestamp createdAt;
    private String roomType; // 추가
    private String roomId; // 추가
    private Integer targetUserId; // 추가
    private String time; // 추가
    private String senderName; // HTML 렌더링용
    private boolean isMyMessage; // HTML 렌더링용
    private String formattedCreatedAt; // HTML 렌더링용		
}