package data.domain;

import lombok.Data;

@Data  // 롬복: getter, setter, equals, hashCode, toString 자동 생성
public class ChatMessage {
    // 메시지 타입 정의
    public enum MessageType {
        ENTER,  // 입장 메시지
        TALK,   // 일반 대화 메시지
        QUIT    // 퇴장 메시지
    }

    private MessageType type;  // 메시지 타입
    private Integer groupId;   // 그룹 채팅방 ID
    private Integer senderId;  // 발신자 ID
    private Integer receiverId;  // 수신자 ID (1:1 채팅인 경우)
    private String message;    // 메시지 내용
    private String sender;     // 발신자 이름
    private String roomId;     // 채팅방 ID (WebSocket 세션 관리용)
}
