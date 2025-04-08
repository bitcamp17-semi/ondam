package data.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import data.dto.ChatGroupsDto;
import data.dto.JuncChatUsersGroupsDto;
import data.mapper.ChatGroupsMapper;
import data.mapper.JunctionChatUsersGroupsMapper;

@Service
public class ChatGroupsService {

    @Autowired
    private ChatGroupsMapper chatGroupsMapper;

    @Autowired
    private JunctionChatUsersGroupsMapper junctionMapper;

 // C: 그룹 생성
    public void createChatGroup(ChatGroupsDto chatGroup) {
        chatGroupsMapper.createChatGroup(chatGroup);
    }

    // R: 그룹 ID로 조회
    public ChatGroupsDto readChatGroupById(int id) {
        return chatGroupsMapper.readChatGroupById(id);
    }

    // R: 전체 그룹 조회
    public List<ChatGroupsDto> readAllChatGroups() {
        return chatGroupsMapper.readAllChatGroups();
    }

    // U: 그룹 수정
    public void updateChatGroup(ChatGroupsDto chatGroup) {
        chatGroupsMapper.updateChatGroup(chatGroup);
    }

    // D: 그룹 삭제
    public void deleteChatGroup(int id) {
        chatGroupsMapper.deleteChatGroup(id);
    }
    
    public ChatGroupsDto readGroupById(int groupId) {
        return chatGroupsMapper.readGroupById(groupId);
    }
    
    public List<ChatGroupsDto> readGroupsByUserId(int userId) {
        return chatGroupsMapper.readGroupsByUserId(userId);
    }
    
    // R: 사용자가 가입한 그룹 ID 조회
    public List<Integer> readGroupIdsByUserId(int userId) {
        List<JuncChatUsersGroupsDto> junctions = junctionMapper.readJunctionByUserId(userId);
        return junctions.stream()
                .map(JuncChatUsersGroupsDto::getGroupId)
                .collect(Collectors.toList());
    }

    // C: 사용자-그룹 관계 생성
    public void createJunction(int userId, int groupId) {
        if (!junctionMapper.readExistsJunction(userId, groupId)) {
            JuncChatUsersGroupsDto junction = new JuncChatUsersGroupsDto();
            junction.setUserId(userId);
            junction.setGroupId(groupId);
            junctionMapper.createJunction(junction);
        }
    }

    // D: 사용자-그룹 관계 삭제
    public void deleteJunction(int userId, int groupId) {
        JuncChatUsersGroupsDto junction = new JuncChatUsersGroupsDto();
        junction.setUserId(userId);
        junction.setGroupId(groupId);
        junctionMapper.deleteJunction(junction);
    }
}
