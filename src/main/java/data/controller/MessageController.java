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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
            List<MessagesDto> messageList = messageService.readMessagesForReceiver(messageId);
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
            @RequestParam(defaultValue = "LATEST") String category,
            @RequestParam int receiverId) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            List<MessagesDto> messages = messageService.readSearchMessagesByKeyword(keyword, category,receiverId);
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
            MessagesDto msg = messageService.readMessageDetail(messageId);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", msg.getId());
            result.put("title", msg.getTitle());
            result.put("content", msg.getContent());
            result.put("senderId", msg.getSenderId());

            // 날짜 처리
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (msg.getCreatedAt() != null) {
                result.put("createdAtWithTime", dateTimeFormat.format(msg.getCreatedAt()));
            } else {
                result.put("createdAtWithTime", "");
            }
            response.put("status", "ok");
            response.put("result", msg);
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
    @ResponseBody
    public List<Map<String, Object>> readMessagesForReceiver(@RequestParam Integer receiverId,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size,
                                                             @RequestParam(defaultValue = "all") String filter) {
        if (receiverId == null) {
            throw new IllegalArgumentException("receiverId는 필수입니다.");
        }
        List<MessagesDto> messages = messageService.readMessagesForReceiver(receiverId);

        if ("important".equals(filter)) {
            messages = messages.stream()
                    .filter(MessagesDto::isImportant)
                    .collect(Collectors.toList());
        }

        //페이징
        int fromIndex = page*size;
        int toIndex = Math.min(fromIndex+size, messages.size());
        if(fromIndex >= messages.size()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (MessagesDto msg : messages.subList(fromIndex, toIndex)) {
            UsersDto sender = usersService.readUserById(msg.getSenderId());

            Map<String, Object> map = new HashMap<>();
            map.put("id", msg.getId());
            map.put("title",msg.getTitle());
            map.put("content", msg.getContent());
            map.put("senderId", msg.getSenderId());
            map.put("senderName", sender != null ? sender.getName() : "알 수 없음");
            map.put("isRead", msg.isRead());
            map.put("isImportant", msg.isImportant()); // 🔥 이 줄만 추가하면 됨!
            /*map.put("createdAt", msg.getCreatedAt());*/

            SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 포맷팅한 값들
            String dateOnly = null;
            String dateTime = null;

            // msg.getCreatedAt()이 null이 아닌 경우에만 포맷팅
            if (msg.getCreatedAt() != null) {
                dateOnly = dateOnlyFormat.format(msg.getCreatedAt());
                dateTime = dateTimeFormat.format(msg.getCreatedAt());
            }

            // map에 두 값 추가
            map.put("createdAtWithTime", dateTime != null ? dateTime : ""); // 날짜+시간
            map.put("createdAt", dateOnly != null ? dateOnly : ""); // 날짜만



            result.add(map);
        }

        return result;
    }

    @GetMapping("/sent")
    @ResponseBody
    public ResponseEntity<Object> readMessagesBySender(@RequestParam int senderId) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            System.out.println("💬 [readMessagesBySender] senderId = " + senderId);

            List<MessagesDto> messages = messageService.readMessagesBySender(senderId);
            List<Map<String, Object>> result = new ArrayList<>();

            for (MessagesDto msg : messages) {
                System.out.println("📦 messageId: " + msg.getId() + ", receiverId: " + msg.getReceiverId());

                UsersDto receiver = usersService.readUserById(msg.getReceiverId());
                if (receiver == null) {
                    System.out.println("❗ Receiver not found for receiverId = " + msg.getReceiverId());
                }

                Map<String, Object> map = new HashMap<>();
                map.put("id", msg.getId());
                map.put("title", msg.getTitle());
                map.put("content", msg.getContent());
                map.put("senderName", receiver != null ? receiver.getName() : "알 수 없음");
                map.put("createdAt", msg.getCreatedAt());
                map.put("isImportant", msg.isImportant());
                map.put("isRead", true); // 항상 읽음
                map.put("receiverName", receiver != null ? receiver.getName() : "알 수 없음");


                result.add(map);
            }

            response.put("status", "ok");
            response.put("result", result);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/next")
    @ResponseBody
    public ResponseEntity<Object> readNextMessage(@RequestParam int receiverId,
                                                  @RequestParam String currentCreatedAt) {
        Map<String, Object> response = new HashMap<>();
        try {
            Timestamp timestamp = Timestamp.valueOf(currentCreatedAt);
            MessagesDto next = messageService.readNextMessageByReceiver(receiverId, timestamp);
            response.put("status", "ok");
            response.put("result", next);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/prev")
    @ResponseBody
    public ResponseEntity<Object> readPrevMessage(@RequestParam int receiverId,
                                                  @RequestParam String currentCreatedAt) {
        Map<String, Object> response = new HashMap<>();
        try {
            Timestamp timestamp = Timestamp.valueOf(currentCreatedAt);
            MessagesDto prev = messageService.readPrevMessageByReceiver(receiverId, timestamp);
            response.put("status", "ok");
            response.put("result", prev);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}

