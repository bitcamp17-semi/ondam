package data.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import data.dto.ChatGroupsDto;
import data.dto.JuncChatUsersGroupsDto;
import data.mapper.ChatGroupsMapper;
import data.mapper.JunctionChatUsersGroupsMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatGroupsService {

	@Autowired
    private ChatGroupsMapper chatGroupMapper; // 그룹 관련 매퍼

    @Autowired
    private JunctionChatUsersGroupsMapper junctionMapper; // 사용자-그룹 관계 매퍼

    // 그룹 생성 (Create)
    public void createChatGroup(ChatGroupsDto chatGroup) {
        chatGroupMapper.createChatGroup(chatGroup); // 매퍼를 통해 그룹 생성
    }

    // ID로 그룹 조회 (Read)
    public ChatGroupsDto readChatGroupById(int id) {
        return chatGroupMapper.readChatGroupById(id); // 매퍼를 통해 ID로 그룹 조회
    }

    // 모든 그룹 조회 (Read)
    public List<ChatGroupsDto> readAllChatGroups() {
        return chatGroupMapper.readAllChatGroups(); // 매퍼를 통해 모든 그룹 조회
    }

    // 그룹 수정 (Update)
    public void updateChatGroup(ChatGroupsDto chatGroup) {
        chatGroupMapper.updateChatGroup(chatGroup); // 매퍼를 통해 그룹 정보 수정
    }

    // 그룹 삭제 (Delete)
    public void deleteChatGroup(int id) {
        chatGroupMapper.deleteChatGroup(id); // 매퍼를 통해 그룹 삭제
    }

    // 사용자가 속한 그룹 ID 목록 조회 (Read)
    public List<Integer> readGroupIdsByUserId(int userId) {
        // junction 테이블에서 사용자가 속한 그룹 목록 조회
        List<JuncChatUsersGroupsDto> junctions = junctionMapper.readJunctionByUserId(userId);
        // 그룹 ID만 추출하여 반환
        return junctions.stream()
                .map(JuncChatUsersGroupsDto::getGroupId)
                .collect(Collectors.toList());
    }

    // 사용자-그룹 관계 추가 (Create, 그룹 가입)
    public void createJunction(int userId, int groupId) {
        JuncChatUsersGroupsDto junction = new JuncChatUsersGroupsDto();
        junction.setUserId(userId);
        junction.setGroupId(groupId);
        junctionMapper.createJunction(junction); // junction 테이블에 사용자-그룹 관계 추가
    }

    // 사용자-그룹 관계 삭제 (Delete, 그룹 탈퇴)
    public void deleteJunction(int userId, int groupId) {
    	JuncChatUsersGroupsDto junction = new JuncChatUsersGroupsDto();
        junction.setUserId(userId);
        junction.setGroupId(groupId);
        junctionMapper.deleteJunction(junction); // junction 테이블에서 사용자-그룹 관계 삭제
    }
}