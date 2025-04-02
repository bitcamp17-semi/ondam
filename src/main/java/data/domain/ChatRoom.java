package data.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Set;

@Data  // 롬복: getter, setter, equals, hashCode, toString 자동 생성
@Builder  // 롬복: 빌더 패턴 자동 생성
public class ChatRoom {
    private String roomId;  // 채팅방 고유 ID
    private Set<WebSocketSession> sessions;  // 해당 채팅방에 연결된 WebSocket 세션들

    // 새로운 채팅방 생성 메서드
    public static ChatRoom create(String roomId) {
        return ChatRoom.builder()
                .roomId(roomId)
                .sessions(new HashSet<>())  // 빈 세션 집합으로 초기화
                .build();
    }
}
