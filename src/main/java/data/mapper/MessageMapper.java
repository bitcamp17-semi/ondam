package data.mapper;

import data.dto.MessagesDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface MessageMapper {
    // 특정 사용자가 받은 메시지 조회 (Read)
    List<MessagesDto> readMessagesForReceiver(int receiverId);

    // 메시지 읽음 처리 (Update)
    void updateMessageAsRead(int id);

    // 마지막 방문 시간 업데이트 (Update)
    void updateUserLastVisitTime(int userId);

    // 메시지 삭제 (Delete)
    void deleteMessageById(int id);

    // 새로운 메시지 생성 (Create)
    void createMessage(MessagesDto message);

    // 대상 필터링 및 검색
    List<MessagesDto> readMessages(Map<String, Object> params);

    MessagesDto readMessageDetail(int id);
}
