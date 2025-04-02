package data.controller;

import data.dto.ChatGroupsDto;
import data.dto.ChatLogDto;
import data.service.ChatGroupsService;
import data.service.ChatLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatGroupsService chatGroupsService;
    private final ChatLogService chatLogService;

    // 그룹 채팅방 조회
    @GetMapping("/group/{groupId}")
    public String getGroupChat(@PathVariable("groupId") int groupId, Model model) {
        ChatGroupsDto group = chatGroupsService.getGroupById(groupId);
        List<ChatLogDto> logs = chatLogService.getLogsByGroupWithPagination(groupId, 10, 0); // 첫 페이지 10개 메시지

        model.addAttribute("group", group);
        model.addAttribute("logs", logs);

        return "layout/chat"; // layout/chat.html 템플릿 반환
    }

    // 개인 채팅방 조회
    @GetMapping("/private")
    public String getPrivateChat(
            @RequestParam("user1") int user1,
            @RequestParam("user2") int user2,
            Model model) {

        List<ChatLogDto> logs = chatLogService.getPrivateMessagesWithPagination(user1, user2, 10, 0); // 첫 페이지 10개 메시지

        model.addAttribute("logs", logs);

        return "layout/chat"; // layout/chat.html 템플릿 반환
    }

    // 새로운 그룹 생성 요청 처리
    @PostMapping("/group")
    public String createGroup(@RequestBody ChatGroupsDto group) {
        chatGroupsService.createGroup(group);
        return "redirect:/chat";
    }

    // 사용자를 그룹에 추가 요청 처리
    @PostMapping("/group/{groupId}/user/{userId}")
    public String addUserToGroup(@PathVariable("groupId") int groupId, @PathVariable("userId") int userId) {
        chatGroupsService.addUserToGroup(userId, groupId);
        return "redirect:/chat/group/" + groupId;
    }
}
