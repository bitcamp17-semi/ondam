package data.controller;

import data.dto.ChatGroupsDto;
import data.dto.ChatLogDto;
import data.dto.ChatRoomData;
import data.dto.UsersDto;
import data.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // 채팅 메인 페이지
    @GetMapping("/main")
    public String chatMain(HttpSession session, Model model,
                           @RequestParam(value = "activeTab", required = false) String activeTab) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
        	userId = 1;
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }

        // 사용자가 속한 그룹 목록 (chatGroupsList로 이름 변경)
        List<ChatGroupsDto> groups = chatService.getAllGroupsWithLastMessages(userId);
        model.addAttribute("chatGroupsList", groups);

        // 모든 사용자 목록 (contacts로 추가)
        List<UsersDto> contacts = chatService.getAllUsersExceptCurrent(userId);
        model.addAttribute("contacts", contacts);

        // 열린 채팅 목록 (세션에서 관리)
        List<Integer> openChats = (List<Integer>) session.getAttribute("openChats");
        if (openChats == null) {
            openChats = new ArrayList<>();
        }
        model.addAttribute("openChats", openChats);

        // 첫 번째 채팅방 데이터
        ChatRoomData firstChatData = null;
        if (!openChats.isEmpty()) {
            Integer firstChatId = openChats.get(0);
            ChatGroupsDto firstChat = chatService.getGroupById(firstChatId);
            if (firstChat != null) {
                firstChatData = new ChatRoomData();
                firstChatData.setRoomId(firstChatId);
                firstChatData.setRoomName(firstChat.getName());
                firstChatData.setRoomType("GROUP"); // 그룹 채팅으로 설정
                List<ChatLogDto> messages = chatService.getGroupMessages(firstChatId);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                for (ChatLogDto msg : messages) {
                    msg.setMyMessage(msg.getSenderId() == userId);
                    if (msg.getCreatedAt() != null) {
                        msg.setFormattedCreatedAt(sdf.format(msg.getCreatedAt()));
                    } else {
                        msg.setFormattedCreatedAt("");
                    }
                }
                firstChatData.setMessages(messages);
            }
        }

        // 불필요한 블록 제거: 이미 위에서 setMyMessage를 설정했으므로 중복 작업 불필요
        /*
        if (firstChatData != null) {
            for (Message msg : firstChatData.getMessages()) {
                msg.setIsMyMessage(msg.getSenderId().equals(userId));
            }
        }
        */

        model.addAttribute("firstChatData", firstChatData);

        // 사용자 정보
        UsersDto user = chatService.getUserById(userId);
        if (user != null) {
            session.setAttribute("user", user);
        }
        
        List<ChatGroupsDto> chatGroupsList = chatService.getAllGroupsWithLastMessages(userId);
        model.addAttribute("chatGroupsList", chatGroupsList);
        model.addAttribute("userId", userId);
        model.addAttribute("activeTab", activeTab != null ? activeTab : "chats");
        return "chat/chatmain";
    }

    // 채팅방 열기
    @PostMapping("/openChat")
    public String openChat(HttpSession session, @RequestParam(value = "chatId") Integer chatId) {
        List<Integer> openChats = (List<Integer>) session.getAttribute("openChats");
        if (openChats == null) {
            openChats = new ArrayList<>();
        }
        if (!openChats.contains(chatId)) {
            openChats.add(chatId);
            session.setAttribute("openChats", openChats);
        }
        return "redirect:/chat/main";
    }

    // 채팅방 닫기
    @PostMapping("/closeChat")
    public String closeChat(HttpSession session, @RequestParam(value = "chatId") Integer chatId) {
        List<Integer> openChats = (List<Integer>) session.getAttribute("openChats");
        if (openChats != null) {
            openChats.remove(chatId);
            session.setAttribute("openChats", openChats);
        }
        return "redirect:/chat/main";
    }
    
    @PostMapping("/switchChat")
    public String switchChat(HttpSession session,@RequestParam("chatId") String chatId, RedirectAttributes redirectAttributes) {
        // 세션에서 openChats 가져오기
        List<String> openChats = (List<String>) session.getAttribute("openChats");
        if (openChats == null) {
            openChats = new ArrayList<>();
        }

        // chatId가 openChats에 있는지 확인
        if (!openChats.contains(chatId)) {
            openChats.add(chatId);
        }

        // 선택한 chatId를 세션에 저장 (활성화된 채팅방으로 설정)
        session.setAttribute("activeChatId", chatId);
        session.setAttribute("openChats", openChats);

        // 리다이렉트 시 activeTab 유지
        redirectAttributes.addAttribute("activeTab", session.getAttribute("activeTab"));
        return "redirect:/chat/main";
    }   

    @PostMapping("/invite")
    public String inviteUserToGroup(
            @RequestParam("groupId") Integer groupId,
            @RequestParam("userId") Integer userId,
            @RequestParam(value = "activeTab", defaultValue = "chats") String activeTab,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        Integer currentUserId = (Integer) session.getAttribute("userId");
        if (currentUserId == null) {
            return "redirect:/login";
        }
        try {
            chatService.createGroupUser(userId, groupId);
            redirectAttributes.addFlashAttribute("message", "사용자가 그룹에 초대되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        redirectAttributes.addAttribute("activeTab", activeTab);
        return "redirect:/chat/main";
    }
    
    //그룹 생성
    @PostMapping("/createGroup")
    public String createGroup(
            @RequestParam("groupName") String groupName,
            @RequestParam(value = "invitedUserIds", required = false) List<Long> invitedUserIds,
            @RequestParam(value = "activeTab", defaultValue = "chats") String activeTab,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            // 그룹 이름 유효성 검사
            if (groupName == null || groupName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "그룹 이름은 필수입니다.");
                redirectAttributes.addAttribute("activeTab", activeTab);
                return "redirect:/chat/main";
            }
            chatService.createGroup(groupName, Long.valueOf(userId), invitedUserIds);
            redirectAttributes.addFlashAttribute("message", "그룹 채팅이 생성되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        redirectAttributes.addAttribute("activeTab", activeTab);
        return "redirect:/chat/main";
    }
    
    // 개인 채팅 생성
    @PostMapping("/createPrivateChat")
    public String createPrivateChat(
            @RequestParam("targetUserId") Integer targetUserId,
            @RequestParam(value = "activeTab", defaultValue = "chats") String activeTab,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            // 자기 자신과의 채팅 방지
            if (userId.equals(targetUserId)) {
                redirectAttributes.addFlashAttribute("error", "자기 자신과는 채팅할 수 없습니다.");
                redirectAttributes.addAttribute("activeTab", activeTab);
                return "redirect:/chat/main";
            }
            Integer chatId = chatService.createPrivateChat(Long.valueOf(userId), Long.valueOf(targetUserId));
            // 열린 채팅 목록에 추가
            List<Integer> openChats = (List<Integer>) session.getAttribute("openChats");
            if (openChats == null) {
                openChats = new ArrayList<>();
            }
            if (!openChats.contains(chatId)) {
                openChats.add(chatId);
                session.setAttribute("openChats", openChats);
            }
            redirectAttributes.addFlashAttribute("message", "개인 채팅이 생성되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        redirectAttributes.addAttribute("activeTab", activeTab);
        return "redirect:/chat/main";
    }
}