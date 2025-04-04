package data.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 채팅 메시지 전송을 위한 DTO 클래스
 * 실시간 메시지 송수신에 사용됨
 */
@Getter
@Setter
@ToString
public class ChatMessage {
    /**
     * 메시지 타입 열거형
     * ENTER: 입장, TALK: 일반 대화, LEAVE: 퇴장
     */
    public enum MessageType {
        ENTER, TALK, LEAVE
    }
    
    private MessageType type;   // 메시지 타입
    private Integer groupId;        // 채팅 그룹 ID
    private int senderId;       // 발신자 ID
    private int receiverId;     // 수신자 ID
    private String senderName;  // 발신자 이름
    private String message;     // 메시지 내용
    private String file;        // 첨부 파일 경로
}
