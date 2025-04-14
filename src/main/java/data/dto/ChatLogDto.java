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
    private Timestamp createdAt;
    private String roomType; 
    private Integer roomId; 
    private Integer targetUserId; 
    private String time; 
    private String senderName; // HTML 렌더링용
    private boolean isMyMessage; // HTML 렌더링용
    private String formattedCreatedAt; // HTML 렌더링용
    private String file; // 파일명 저장 필드
    private String fileUrl; // 파일 URL (네이버 클라우드에 저장된 경로)
    private String fileName; // 원본 파일 이름
    private String fileType; // 업로드 파일 타입
}