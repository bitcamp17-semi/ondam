package data.controller;

import data.dto.ChatLogDto;
import data.service.ChatLogService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/pagination")
public class PageController {

    private final ChatLogService chatLogService;

 // 그룹 채팅 로그 페이징 처리 API
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ChatLogDto>> getGroupChatLogsPaginated(
            @PathVariable int groupId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        List<ChatLogDto> logs = chatLogService.getLogsByGroupWithPagination(groupId, page, size);
        return ResponseEntity.ok(logs);
    }

    // 개인 채팅 로그 페이징 처리 API
    @GetMapping("/private")
    public ResponseEntity<List<ChatLogDto>> getPrivateChatLogsPaginated(
            @RequestParam(value = "user1") int user1,
            @RequestParam(value = "user2") int user2,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        List<ChatLogDto> logs = chatLogService.getPrivateMessagesWithPagination(user1, user2, page, size);
        return ResponseEntity.ok(logs);
    }
}
