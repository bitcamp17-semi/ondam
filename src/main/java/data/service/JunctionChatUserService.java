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

    // C: 사용자-그룹 관계 생성
    public void createJunction(JuncChatUsersGroupsDto junction) {
        juncChatUsersGroupsMapper.createJunction(junction);
    }

    // R: 사용자 ID로 관계 조회
    public List<JuncChatUsersGroupsDto> readJunctionByUserId(int userId) {
        return juncChatUsersGroupsMapper.readJunctionByUserId(userId);
    }

    // R: 그룹 ID로 사용자 목록 조회
    public List<JuncChatUsersGroupsDto> readUsersByGroupId(int groupId) {
        return juncChatUsersGroupsMapper.readUsersByGroupId(groupId);
    }

    // D: 관계 삭제
    public void deleteJunction(JuncChatUsersGroupsDto junction) {
        juncChatUsersGroupsMapper.deleteJunction(junction);
    }
    
    //사용자 초대    
    public void createUserGroupLink(JuncChatUsersGroupsDto dto) {
    	juncChatUsersGroupsMapper.createUserGroupLink(dto);
    }
}
