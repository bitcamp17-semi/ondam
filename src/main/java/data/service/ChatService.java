package data.service;

import data.dto.ChatGroupsDto;
import data.dto.ChatLogDto;
import data.dto.UsersDto;
import data.mapper.ChatGroupsMapper;
import data.mapper.ChatLogMapper;
import data.mapper.UsersMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private ChatGroupsMapper chatGroupsMapper;

    @Autowired
    private ChatLogMapper chatLogMapper;

    @Autowired
    private UsersMapper usersMapper;

    // 사용자가 속한 그룹 목록과 마지막 메시지 조회
    public List<ChatGroupsDto> getAllGroupsWithLastMessages(Integer userId) {
        return chatGroupsMapper.readAllGroupsWithLastMessages(Long.valueOf(userId));
    }

    // 특정 그룹 정보 조회
    public ChatGroupsDto getGroupById(Integer groupId) {
        return chatGroupsMapper.readGroupById(groupId);
    }

    // 그룹 채팅 메시지 조회
    public List<ChatLogDto> getGroupMessages(Integer groupId) {
        List<ChatLogDto> messages = chatLogMapper.readAllLogsByGroupId(Long.valueOf(groupId));
        for (ChatLogDto msg : messages) {
            UsersDto sender = usersMapper.readUserById(msg.getSenderId());
            if (sender != null) {
                msg.setSenderName(sender.getName());
            }
        }
        return messages;
    }

    // 1:1 채팅 메시지 조회
    public List<ChatLogDto> getPrivateMessages(Integer userId, Integer chatId) {
        Map<String, Integer> params = new HashMap<>();
        params.put("userId", userId);
        params.put("chatId", chatId);
        List<ChatLogDto> messages = chatLogMapper.readAllPrivateLogs(params);
        for (ChatLogDto msg : messages) {
            UsersDto sender = usersMapper.readUserById(msg.getSenderId());
            if (sender != null) {
                msg.setSenderName(sender.getName());
            }
        }
        return messages;
    }

    // 채팅 메시지 저장
    public void saveChatMessage(ChatLogDto chatLogDto) {
        if (chatLogDto == null) {
            logger.error("ChatLogDto is null");
            throw new IllegalArgumentException("ChatLogDto cannot be null");
        }
        if (chatLogDto.getSenderId() == null) {
            logger.error("Sender ID is null in ChatLogDto: {}", chatLogDto);
            throw new IllegalArgumentException("Sender ID cannot be null");
        }
        if (chatLogDto.getMessage() == null || chatLogDto.getMessage().trim().isEmpty()) {
            logger.error("Message content is empty in ChatLogDto: {}", chatLogDto);
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        if (chatLogDto.getRoomId() == null) {
            logger.error("Room ID is null in ChatLogDto: {}", chatLogDto);
            throw new IllegalArgumentException("Room ID cannot be null");
        }
        logger.info("Saving ChatLogDto: {}", chatLogDto);
        chatLogMapper.createChatLog(chatLogDto);
        logger.info("ChatLogDto saved successfully");
    }

    // 사용자 정보 조회
    public UsersDto getUserById(Integer userId) {
        return usersMapper.readUserById(userId);
    }

    // 사용자가 속한 그룹의 사용자 목록 조회
    public List<Long> getGroupUserIds(Integer groupId) {
        return chatGroupsMapper.readGroupUserIds(Long.valueOf(groupId));
    }

    // 현재 사용자를 제외한 모든 사용자 목록 조회 (contacts용)
    public List<UsersDto> getAllUsersExceptCurrent(Integer userId) {
        List<UsersDto> allUsers = usersMapper.readAllActiveUsers();
        if (allUsers == null || userId == null) {
            return List.of();
        }
        return allUsers.stream()
                .filter(user -> user.getId() != userId)
                .collect(Collectors.toList());
    }
    
    public void createGroupUser(Integer userId, Integer groupId) {
        if (userId == null || groupId == null) {
            logger.error("User ID or Group ID is null: userId={}, groupId={}", userId, groupId);
            throw new IllegalArgumentException("User ID and Group ID cannot be null");
        }
        // 그룹 존재 여부 확인
        ChatGroupsDto group = chatGroupsMapper.readGroupById(groupId);
        if (group == null) {
            logger.error("Group not found: groupId={}", groupId);
            throw new IllegalArgumentException("Group not found");
        }
        // 사용자 존재 여부 확인
        UsersDto user = usersMapper.readUserById(userId);
        if (user == null) {
            logger.error("User not found: userId={}", userId);
            throw new IllegalArgumentException("User not found");
        }
        // 이미 그룹에 있는지 확인
        List<Long> groupUserIds = chatGroupsMapper.readGroupUserIds(Long.valueOf(groupId));
        if (groupUserIds.contains(Long.valueOf(userId))) {
            logger.warn("User already in group: userId={}, groupId={}", userId, groupId);
            return; // 이미 있으면 추가하지 않음
        }
        // 그룹에 사용자 추가
        chatGroupsMapper.createGroupUser(Long.valueOf(userId), Long.valueOf(groupId));
        logger.info("User invited to group: userId={}, groupId={}", userId, groupId);
    }
}