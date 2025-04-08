package data.controller;

import data.dto.ChatGroupsDto;
import data.dto.ChatLogDto;
import data.dto.JuncChatUsersGroupsDto;
import data.dto.UsersDto;
import data.service.ChatGroupsService;
import data.service.ChatLogService;
import data.service.JunctionChatUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatGroupsService chatGroupsService;

    @Autowired
    private ChatLogService chatLogService;
    
    @Autowired
    private JunctionChatUserService junctionChatUserservice;
    
    
    
    // 채팅 그룹 메인 페이지
    @GetMapping("/main")
    public String chatMain(Model model, HttpSession session) {
        UsersDto loginUser = (UsersDto) session.getAttribute("user");
        if (loginUser == null) {
            return "redirect:/login"; // 로그인 되어 있지 않으면 로그인 페이지로
        }

        List<ChatGroupsDto> groupList = chatGroupsService.readGroupsByUserId(loginUser.getId());
        model.addAttribute("groupList", groupList);
        return "chat/chatmain";
    }

    // 채팅 그룹 생성 폼
    @GetMapping("/create-group")
    public String createGroupForm() {
        return "chat/create_group";
    }

    // 채팅 그룹 생성 처리
    @PostMapping("/create-group")
    public String createGroup(@RequestParam("name") String name, HttpSession session) {
        UsersDto loginUser = (UsersDto) session.getAttribute("user");
        System.out.println("로그인 유저: " + loginUser); // 디버깅용

        if (loginUser == null) {
            return "redirect:/login";
        }

        ChatGroupsDto newGroup = new ChatGroupsDto();
        newGroup.setName(name);
        newGroup.setCreatedAt(LocalDate.now());

        // 그룹 생성 (insert 및 id auto-generation)
        chatGroupsService.createChatGroup(newGroup);

        // 생성된 그룹 ID 확인
        int groupId = newGroup.getId();

        // 유저-그룹 연결 생성
        chatGroupsService.createJunction(loginUser.getId(), groupId);

        return "redirect:/chat/main";
    }


    @GetMapping("/groups")
    public String readGroupList(Model model) {
        List<ChatGroupsDto> groupList = chatGroupsService.readAllChatGroups();
        model.addAttribute("groupList", groupList);
        return "chat/chatmain"; // templates/chatmain.html
    }

    // 그룹 채팅방 입장
    @GetMapping("/group/{groupId}")
    public String groupChat(@PathVariable("groupId") int groupId, Model model, HttpSession session) {
        UsersDto loginUser = (UsersDto) session.getAttribute("user");
        if (loginUser == null) {
            return "redirect:/login";
        }

        ChatGroupsDto groupInfo = chatGroupsService.readGroupById(groupId);
        List<ChatLogDto> chatLogs = chatLogService.readChatLogsByGroupId(groupId);

        model.addAttribute("groupInfo", groupInfo);
        model.addAttribute("chatLogs", chatLogs);
        model.addAttribute("loginUser", loginUser); // ⭐️ 이 줄을 추가해야 함!

        return "chat/group_chat";
    }
    
    //그룹 채팅방에 사용자 초대
    @PostMapping("/invite")
    @ResponseBody
    public String inviteUserToGroup(@RequestParam int userId,
                                     @RequestParam int groupId) {
        JuncChatUsersGroupsDto dto = new JuncChatUsersGroupsDto();
        dto.setUserId(userId);
        dto.setGroupId(groupId);
        junctionChatUserservice.createUserGroupLink(dto);
        return "success";
    }
}
