package data.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import data.dto.ChatLogDto;

import java.util.List;

@Mapper  // MyBatis 매퍼 인터페이스 지정
public interface ChatLogMapper {
	// 그룹 ID로 채팅 로그 조회
    public List<ChatLogDto> readByGroupId(@Param("groupId") int groupId);
    // 두 사용자 간의 개인 메시지 조회
    public List<ChatLogDto> readPrivateMessages(@Param("user1Id") int user1Id, @Param("user2Id") int user2Id);
    // 채팅 로그 저장
    public void createChatLog(ChatLogDto chatLog);
    // 메시지를 읽음 상태로 변경
    public void readChat(@Param("receiverId") int receiverId, @Param("senderId") int senderId);
    // 그룹 ID로 채팅 로그 조회 (페이징 처리)
    public List<ChatLogDto> readByGroupIdWithPagination(@Param("groupId") int groupId,@Param("pageSize") int pageSize,
    													@Param("offset") int offset);
    // 두 사용자 간의 개인 메시지 조회 (페이징 처리)
    public List<ChatLogDto> readPrivateMessagesWithPagination(@Param("user1Id") int user1Id,@Param("user2Id") int user2Id,
                                                       @Param("pageSize") int pageSize,@Param("offset") int offset);
}
