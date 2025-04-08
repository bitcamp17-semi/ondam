package data.controller;

import data.dto.MessagesDto;
import data.dto.UsersDto;
import data.service.MessageService;
import data.service.UsersService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    UsersService usersService;

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/inbox")
    public String inboxPage(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login"; // 로그인 안 된 경우 처리
        }
        model.addAttribute("userId", userId);
        return "layout/message/message_inbox";  // 확장자 .html 생략
    }
    // 새로운 메시지 확인 (읽지 않은 메시지 여부)
    @GetMapping("/unread")
    public ResponseEntity<Object> readCountNewMessages(@RequestParam int receiverId) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            boolean hasNewMessages = messageService.readCountUnreadMessages(receiverId);
            response.put("status", "ok");
            response.put("result", hasNewMessages);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 특정 유저의 받은 메시지 목록 반환
    @GetMapping("/receiver/{receiverId}")
    public ResponseEntity<Object> getMessagesForReceiver(@PathVariable int receiverId) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            List<MessagesDto> messages = messageService.readMessagesForReceiver(receiverId);
            response.put("status", "ok");
            response.put("result", messages);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 메시지를 읽음 처리
    @PostMapping("/read/{messageId}")
    public ResponseEntity<Object> markMessageAsRead(@PathVariable int messageId) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            messageService.markMessageAsRead(messageId);
            response.put("status", "ok");
            response.put("message", "Message has been marked as read.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 마지막 방문 시간 업데이트
    @PostMapping("/visit/{userId}")
    public ResponseEntity<Object> updateLastVisit(@PathVariable int userId) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            messageService.updateLastVisit(userId);
            response.put("status", "ok");
            response.put("message", "Last visit time has been updated.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 메시지 삭제
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Object> deleteMessage(@PathVariable int messageId) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            messageService.deleteMessage(messageId);
            response.put("status", "ok");
            response.put("message", "Message has been deleted.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 새로운 메시지 생성
    @PostMapping("/createMessage")
    public ResponseEntity<Object> createMessage(@RequestBody MessagesDto messageDto,
                                                HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Integer senderId=(Integer)session.getAttribute("userId");
            if(senderId==null) {
                response.put("status", "fail");
                response.put("message","로그인이 필요합니다");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            messageDto.setSenderId(senderId);
            messageService.createMessage(messageDto);
            response.put("status", "ok");
            response.put("message", "Message has been created.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/departments")
    @ResponseBody
    public List<String> readAllDepartment() {
        return messageService.readAllDepartment();
    }

    @GetMapping("/userByDepartment")
    @ResponseBody
    public List<UsersDto> readUsersByDepartment(@RequestParam String department) {
        return messageService.readUsersByDepartment(department);
    }

    // 검색 API 추가
    @GetMapping("/search")
    public ResponseEntity<Object> searchMessages(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "LATEST") String category) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            List<MessagesDto> messages = messageService.readSearchMessagesByKeyword(keyword, category);
            response.put("status", "ok");
            response.put("result", messages);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/{messageId}")
    public ResponseEntity<Object> getMessageDetail(@PathVariable int messageId) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            MessagesDto message = messageService.readMessageDetail(messageId);
            response.put("status", "ok");
            response.put("result", message);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/important/{messageId}")
    public ResponseEntity<Object> markMessageAsImportant(
            @PathVariable int messageId,
            @RequestParam boolean isImportant) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            messageService.markMessageAsImportant(messageId, isImportant);
            response.put("status", "ok");
            response.put("message", "Message importance updated.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/users/name")
    @ResponseBody
    public String getUserName(@RequestParam int id) {
        UsersDto user = usersService.readUserById(id);
        return (user != null) ? user.getName() : "알 수 없음";
    }

    @GetMapping("/list")
    public List<Map<String, Object>> readMessagesForReceiver(@RequestParam Integer receiverId) {
        if (receiverId == null) {
            throw new IllegalArgumentException("receiverId는 필수입니다.");
        }
        List<MessagesDto> messages = messageService.readMessagesForReceiver(receiverId);

        List<Map<String, Object>> result = new ArrayList<>();

        for (MessagesDto msg : messages) {
            UsersDto sender = usersService.readUserById(msg.getSenderId());

            Map<String, Object> map = new HashMap<>();
            map.put("id", msg.getId());
            map.put("content", msg.getContent());
            map.put("senderId", msg.getSenderId());
            map.put("senderName", sender != null ? sender.getName() : "알 수 없음");
            map.put("isRead", msg.isRead());
            map.put("createdAt", msg.getCreatedAt());

            result.add(map);
        }

        return result;
    }



}

