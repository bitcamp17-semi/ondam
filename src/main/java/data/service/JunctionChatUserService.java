package data.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import data.dto.JuncChatUsersGroupsDto;
import data.mapper.JunctionChatUsersGroupsMapper;

@Service
public class JunctionChatUserService {

    @Autowired
    private JunctionChatUsersGroupsMapper juncChatUsersGroupsMapper;

    // 사용자-그룹 관계 생성 (Create)
    public void createJunction(JuncChatUsersGroupsDto junction) {
    	juncChatUsersGroupsMapper.createJunction(junction);
    }

    // 사용자-그룹 관계 조회 (Read)
    public List<JuncChatUsersGroupsDto> readJunctionByUserId(int userId) {
        return juncChatUsersGroupsMapper.readJunctionByUserId(userId);
    }

    // 그룹에 속한 사용자 조회 (Read)
    public List<JuncChatUsersGroupsDto> readUsersByGroupId(int groupId) {
        return juncChatUsersGroupsMapper.readUsersByGroupId(groupId);
    }

    // 사용자-그룹 관계 삭제 (Delete)
    public void deleteJunction(JuncChatUsersGroupsDto junction) {
    	juncChatUsersGroupsMapper.deleteJunction(junction);
    }
}