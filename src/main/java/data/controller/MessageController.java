package data.controller;

import data.dto.MessagesDto;
import data.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }
    // 새로운 메시지 확인 (읽지 않은 메시지 여부)
    @GetMapping("/unread")
    public ResponseEntity<Object> hasNewMessages(@RequestParam int receiverId) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            //boolean hasNewMessages = messageService.(알람 관련 메서드 넣기)(receiverId);
            response.put("status", "ok");
            //response.put("result", hasNewMessages);
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
            List<MessagesDto> messages = messageService.getMessagesForReceiver(receiverId);
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
    @PostMapping("/")
    public ResponseEntity<Object> createMessage(@RequestBody MessagesDto messageDto) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
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

    // 검색 API 추가
    @GetMapping("/search")
    public ResponseEntity<Object> searchMessages(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "LATEST") String category) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            List<MessagesDto> messages = messageService.readMessages(keyword, category);
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


}

