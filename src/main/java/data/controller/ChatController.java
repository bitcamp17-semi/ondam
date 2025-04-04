package data.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import data.dto.ChatGroupsDto;
import data.dto.ChatLogDto;
import data.dto.UsersDto;
import data.service.ChatGroupsService;
import data.service.ChatLogService;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatGroupsService chatGroupsService; // 그룹 관련 서비스

    @Autowired
    private ChatLogService chatLogService; // 채팅 로그 관련 서비스

    /**
     * 채팅 메인 페이지 표시 (Read, 사용자의 그룹 목록 보기)
     */
    @GetMapping
    public String readChatPage(HttpSession session, Model model) {
        // 세션에서 현재 로그인한 사용자 정보 가져오기
        UsersDto user = (UsersDto) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }

        // 사용자가 속한 그룹 목록 조회
        List<Integer> groupIds = chatGroupsService.readGroupIdsByUserId(user.getId());
        List<ChatGroupsDto> groups = groupIds.stream()
            .map(chatGroupsService::readChatGroupById)
            .filter(Objects::nonNull) // null 필터링 추가
            .collect(Collectors.toList());

        // 모델에 사용자 정보와 그룹 목록 추가
        model.addAttribute("user", user);
        model.addAttribute("groups", groups);
        return "chat"; // chat.html 템플릿 렌더링
    }

    /**
     * 모든 그룹 조회 - 관리자 또는 전체 그룹 목록용
     */
    @GetMapping("/groups")
    public String readAllChatGroups(HttpSession session, Model model) {
        // 세션에서 사용자 정보 확인 (추가)
        UsersDto user = (UsersDto) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user); // ⭐️ 필수 추가
        model.addAttribute("chatGroups", chatGroupsService.readAllChatGroups());
        return "chat/chatmain";
    }

    
    //그룹 생성 페이지 표시 (Create 준비)    
    @GetMapping("/groups/new")
    public String createGroupPage(Model model) {
        model.addAttribute("chatGroup", new ChatGroupsDto());
        return "chat/create_group";
    }

    
     // 그룹 생성 처리 (Create)     
    @PostMapping("/groups/new")
    public String createGroup(@ModelAttribute ChatGroupsDto chatGroup, HttpSession session, RedirectAttributes redirectAttr) {
        UsersDto user = (UsersDto) session.getAttribute("user"); // 현재 사용자 정보 가져오기
        if (user == null) {
            return "redirect:/login";
        }
        
        chatGroupsService.createChatGroup(chatGroup); // 그룹 생성
        chatGroupsService.createJunction(user.getId(), chatGroup.getId()); // 생성한 사용자를 그룹에 추가
        
        redirectAttr.addFlashAttribute("success", "그룹 생성 완료!");
        return "redirect:/chat/groups/" + chatGroup.getId(); // 생성된 그룹 채팅방으로 이동
    }

    
    //그룹 채팅 페이지 표시 (Read)     
    @GetMapping("/groups/{groupId}")
    public String readGroupChat(@PathVariable int groupId, HttpSession session, Model model) {
        UsersDto user = (UsersDto) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }

        try {
            // 그룹 정보와 채팅 로그 조회
            ChatGroupsDto group = chatGroupsService.readChatGroupById(groupId);
            List<ChatLogDto> chatLogs = chatLogService.readChatLogsByGroupId(groupId);

            // 사용자가 속한 그룹 목록 조회 (사이드바 표시용)
            List<Integer> groupIds = chatGroupsService.readGroupIdsByUserId(user.getId());
            List<ChatGroupsDto> groups = groupIds.stream()
                    .map(chatGroupsService::readChatGroupById)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 모델에 필요한 데이터 추가
            model.addAttribute("user", user);
            model.addAttribute("group", group);
            model.addAttribute("chatLogs", chatLogs);
            model.addAttribute("groups", groups);
            return "group_chat"; // group_chat.html 템플릿 렌더링
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "그룹을 찾을 수 없습니다.");
            return "error_page"; // 에러 페이지 표시
        }
    }

    
    //그룹 수정 페이지 표시 (Update 준비)     
    @GetMapping("/groups/{groupId}/edit")
    public String updateGroupForm(@PathVariable int groupId, Model model) {
        model.addAttribute("chatGroup", chatGroupsService.readChatGroupById(groupId));
        return "group_chat"; // 그룹 수정 양식 템플릿 렌더링
    }

    
    //그룹 수정 처리 (Update)     
    @PutMapping("/groups/{groupId}")
    public String updateGroup(@PathVariable int groupId, @ModelAttribute ChatGroupsDto chatGroup) {
        chatGroup.setId(groupId);
        chatGroupsService.updateChatGroup(chatGroup);
        return "redirect:/chat/groups/" + groupId; // 수정된 그룹 페이지로 리다이렉트
    }

    
    //그룹 삭제 처리 (Delete)     
    @DeleteMapping("/groups/{groupId}")
    public String deleteGroup(@PathVariable int groupId) {
        chatGroupsService.deleteChatGroup(groupId);
        return "redirect:/chat/groups"; // 그룹 목록 페이지로 리다이렉트
    }

    
    //그룹 가입 처리 (Create - 사용자-그룹 관계)     
    @GetMapping("/groups/join/{groupId}")
    public String joinGroup(@PathVariable int groupId, HttpSession session) {
        UsersDto user = (UsersDto) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        chatGroupsService.createJunction(user.getId(), groupId); // 사용자-그룹 관계 추가
        return "redirect:/chat"; // 채팅 메인 페이지로 리다이렉트
    }

    
     //그룹 탈퇴 처리 (Delete - 사용자-그룹 관계)     
    @GetMapping("/groups/leave/{groupId}")
    public String leaveGroup(@PathVariable int groupId, HttpSession session) {
        UsersDto user = (UsersDto) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        chatGroupsService.deleteJunction(user.getId(), groupId); // 사용자-그룹 관계 삭제
        return "redirect:/chat"; // 채팅 메인 페이지로 리다이렉트
    }
}
