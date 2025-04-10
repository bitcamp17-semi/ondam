package data.mapper;

import data.dto.MessagesDto;
import data.dto.UsersDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface MessageMapper {
    // 특정 사용자가 받은 메시지 조회 (Read)
    List<MessagesDto> readMessagesForReceiver(@Param("receiverId") int receiverId);

    // 메시지 읽음 처리 (Update)
    void updateMessageAsRead(int id);

    // 마지막 방문 시간 업데이트 (Update)
    void updateUserLastVisitTime(int userId);

    // 메시지 삭제 (Delete)
    void deleteMessageById(int id);

    // 새로운 메시지 생성 (Create)
    void createMessage(MessagesDto message);

    // 대상 필터링 및 검색
    List<MessagesDto> readSearchMessagesByKeyword(Map<String, Object> params);

    MessagesDto readMessageDetail(int id);

    void updateMessageImportance(@Param("id") int id, @Param("isImportant") boolean isImportant);

    int readCountUnreadMessages(int receiverId);
    List<String> readAllDepartment();

    List<UsersDto> readUsersByDepartment(String department);

    MessagesDto readNextMessageByReceiver(@Param("receiverId") int receiverId,
                                          @Param("currentCreatedAt") java.sql.Timestamp currentCreatedAt);

    MessagesDto readPrevMessageByReceiver(@Param("receiverId") int receiverId,
                                          @Param("currentCreatedAt") java.sql.Timestamp currentCreatedAt);

    List<MessagesDto> readMessagesBySender(int senderId);



}
