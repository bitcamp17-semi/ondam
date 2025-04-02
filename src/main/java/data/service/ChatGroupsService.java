package data.service;

import data.dto.ChatGroupsDto;
import data.mapper.ChatGroupsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatGroupsService {
    private final ChatGroupsMapper chatGroupsMapper;

    public ChatGroupsDto getGroupById(int id)
    {
        return chatGroupsMapper.readById(id);
    }
    public List<ChatGroupsDto> getGroupsByUserId(int userId)
    {
        return chatGroupsMapper.readByUserId(userId);
    }
    public void createGroup(ChatGroupsDto chatGroup)
    {
        chatGroupsMapper.createGroupChat(chatGroup);
    }
    public void addUserToGroup(int userId, int groupId) 
    {
        chatGroupsMapper.createUserToGroup(userId, groupId);
    }
}
