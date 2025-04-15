package data.controller;

import data.dto.*;
import data.service.ChatService;
import data.service.FileStorageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import config.NaverConfig;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

	private static final String DEFAULT_TAB = "chats";
	private static final String LOGIN_REDIRECT = "redirect:/login";
	private static final String MAIN_PAGE = "chat/chatmain";
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static final String GROUP_ROOM_TYPE = "GROUP";
	private static final String PRIVATE_ROOM_TYPE = "PRIVATE";
	private static final String AUTH_ERROR_MESSAGE = "인증되지 않은 사용자입니다.";
	private static final String SELF_CHAT_ERROR = "자기 자신과는 채팅할 수 없습니다.";
	private static final String GROUP_NAME_REQUIRED = "그룹 이름은 필수입니다.";

	private final ChatService chatService;
	private final FileStorageService fileStorageService;
	private final SimpMessagingTemplate messagingTemplate;
	private final NaverConfig naverConfig;
	
	private String bucketName="bitcamp-semi";//각자 자기꺼 써야함

	/**
	 * 채팅 메인 페이지 렌더링
	 */
	@GetMapping("/main")
    public String chatMain(HttpSession session, Model model,
            @RequestParam(name = "activeTab", defaultValue = DEFAULT_TAB) String activeTab,
            @RequestParam(name = "chatId", required = false) Integer chatId,
            RedirectAttributes redirectAttributes) {
        Integer userId = validateSession(session, redirectAttributes);
        if (userId == null) {
            return LOGIN_REDIRECT;
        }
        initializeChatMainModel(session, model, userId, activeTab, chatId);
        return MAIN_PAGE;
    }

	/**
	 * 채팅방 열기
	 */
	@PostMapping("/openChat")
	public String openChat(HttpSession session,
			@RequestParam(name = "chatId") Integer chatId,
			RedirectAttributes redirectAttributes) {
		return handleSessionAction(session, redirectAttributes, () -> addChatToSession(session, chatId));
	}

	/**
	 * 채팅방 닫기
	 */
	@PostMapping("/closeChat")
	public String closeChat(HttpSession session,
			@RequestParam(name = "chatId") Integer chatId,
			RedirectAttributes redirectAttributes) {
		return handleSessionAction(session, redirectAttributes, () -> removeChatFromSession(session, chatId));
	}

	/**
	 * 그룹에 사용자 초대
	 */
	@PostMapping("/invite")
	public String inviteUserToGroup(@RequestParam(name = "groupId") Integer groupId,
			@RequestParam(name = "userId") Integer userId,
			@RequestParam(name = "activeTab", defaultValue = DEFAULT_TAB) String activeTab,
			HttpSession session, RedirectAttributes redirectAttributes) {
		return handleSessionAction(session, redirectAttributes, () -> {
			chatService.createGroupUser(userId, groupId);
			redirectAttributes.addFlashAttribute("message", "사용자가 그룹에 초대되었습니다.");
			redirectAttributes.addAttribute("activeTab", activeTab);
		});
	}

	/**
	 * 그룹 채팅 생성
	 */
	@PostMapping("/createGroup")
	public String createGroup(@RequestParam(name = "groupName") String groupName,
			@RequestParam(name = "invitedUserIds", required = false) List<Long> invitedUserIds,
			@RequestParam(name = "activeTab", defaultValue = DEFAULT_TAB) String activeTab,
			HttpSession session, RedirectAttributes redirectAttributes) {
		return handleSessionAction(session, redirectAttributes, () -> {
			validateGroupName(groupName);
			Integer userId = getUserIdFromSession(session);
			chatService.createGroup(groupName, Long.valueOf(userId), invitedUserIds);
			redirectAttributes.addFlashAttribute("message", "그룹 채팅이 생성되었습니다.");
			redirectAttributes.addAttribute("activeTab", activeTab);
		});
	}

	/**
	 * 개인 채팅 생성
	 */
	@PostMapping("/createPrivateChat")
	public String createPrivateChat(@RequestParam(name = "targetUserId") Integer targetUserId,
			@RequestParam(name = "activeTab", defaultValue = DEFAULT_TAB) String activeTab,
			HttpSession session, RedirectAttributes redirectAttributes) {
		return handleSessionAction(session, redirectAttributes, () -> {
			validateSelfChat(session, targetUserId);
			Integer newChatId = null;
			Integer userId = getUserIdFromSession(session);
			Integer chatId = chatService.createPrivateChat(Long.valueOf(userId), Long.valueOf(targetUserId));
			addChatToSession(session, chatId);
			redirectAttributes.addFlashAttribute("message", "개인 채팅이 생성되었습니다.");
			redirectAttributes.addAttribute("activeTab", activeTab);
			redirectAttributes.addAttribute("chatId", newChatId);
		});
	}

	/**
	 * 파일 업로드 및 메시지 전송
	 */
	@PostMapping("/uploadFile")
	@ResponseBody
	public ResponseEntity<ChatLogDto> uploadFile(@RequestParam(name = "file") MultipartFile file,
			@RequestParam(name = "roomId") Integer roomId,
			@RequestParam(name = "roomType") String roomType,
			@RequestParam(name = "senderId") Integer senderId,
			@RequestParam(name = "senderName") String senderName,
			HttpSession session) {
		try {
			validateSender(session, senderId);
			String fileUrl = fileStorageService.uploadFile(file);
			ChatLogDto chatLogDto = createChatLogDto(file, roomId, roomType, senderId, senderName, fileUrl);
			chatService.createFileMessage(chatLogDto);
			sendMessageViaWebSocket(chatLogDto, roomId, roomType);
			return ResponseEntity.ok(chatLogDto);
		} catch (IllegalStateException | IllegalArgumentException e) {
			log.error("파일 업로드 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
	}
	
	@GetMapping("/loadChat")
    @ResponseBody
    public ChatRoomData loadChat(@RequestParam("chatId") Integer chatId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        ChatGroupsDto chatGroup = chatService.readGroupById(chatId);
        if (chatGroup == null) {
            throw new IllegalArgumentException("채팅방을 찾을 수 없습니다.");
        }

        ChatRoomData chatRoomData = new ChatRoomData();
        chatRoomData.setRoomId(chatId);

        if ("GROUP".equals(chatGroup.getRoomType())) {
            chatRoomData.setRoomName(chatGroup.getName());
            chatRoomData.setRoomType("GROUP");
            chatRoomData.setMessages(chatService.readGroupMessages(chatId));
        } else if ("PRIVATE".equals(chatGroup.getRoomType())) {
            Long targetUserId = chatGroup.getTargetUserId();
            if (targetUserId == null) {
                throw new IllegalStateException("개인 채팅방의 상대방 ID를 찾을 수 없습니다.");
            }
            UsersDto targetUser = chatService.readUserById(targetUserId.intValue());
            String roomName = targetUser != null ? targetUser.getName() : "알 수 없는 사용자";
            chatRoomData.setRoomName(roomName);
            chatRoomData.setRoomType("PRIVATE");
            chatRoomData.setTargetUserId(targetUserId.intValue());
            chatRoomData.setMessages(chatService.readPrivateMessages(userId, chatId));
        }

        chatRoomData.getMessages().forEach(msg -> {
            msg.setMyMessage(msg.getSenderId().equals(userId));
            msg.setFormattedCreatedAt(msg.getCreatedAt() != null ?
                    new SimpleDateFormat("yyyy-MM-dd HH:mm").format(msg.getCreatedAt()) : "");
        });
        return chatRoomData;
    }

	// ===== Private Methods =====

	private void initializeChatMainModel(HttpSession session, Model model, Integer userId, String activeTab, Integer chatId) {
        List<UsersDto> contacts = chatService.readAllUsersExceptCurrent(userId);
        for (UsersDto contact : contacts) {
            if (contact.getProfileImage() != null && !contact.getProfileImage().isEmpty()) {
                String imageUrl = "https://kr.object.ncloudstorage.com/" + bucketName + "/users/" + contact.getProfileImage();
                contact.setProfileImage(imageUrl);
            }
        }

        model.addAttribute("chatGroupsList", chatService.readAllGroupsWithLastMessages(userId));
        model.addAttribute("contacts", contacts);
        model.addAttribute("userId", userId);
        model.addAttribute("activeTab", activeTab);

        // chatId가 있을 때만 firstChatData 설정
        if (chatId != null) {
            model.addAttribute("firstChatData", prepareFirstChatData(chatId, userId));
        } else {
            model.addAttribute("firstChatData", null);
        }

        UsersDto user = chatService.readUserById(userId);
        session.setAttribute("user", user);
    }

	private ChatRoomData prepareFirstChatData(Integer chatId, Integer userId) {
	    ChatGroupsDto firstChat = chatService.readGroupById(chatId);
	    if (firstChat == null) {
	        return null;
	    }

	    ChatRoomData chatRoomData = new ChatRoomData();
	    chatRoomData.setRoomId(chatId);

	    if ("GROUP".equals(firstChat.getRoomType())) {
	        chatRoomData.setRoomName(firstChat.getName());
	        chatRoomData.setRoomType(GROUP_ROOM_TYPE);
	        chatRoomData.setMessages(chatService.readGroupMessages(chatId));
	    } else if ("PRIVATE".equals(firstChat.getRoomType())) {
	        Long targetUserId = firstChat.getTargetUserId();
	        if (targetUserId == null) {
	            throw new IllegalStateException("개인 채팅방의 상대방 ID를 찾을 수 없습니다.");
	        }
	        UsersDto targetUser = chatService.readUserById(targetUserId.intValue());
	        String roomName = targetUser != null ? targetUser.getName() : "알 수 없는 사용자";
	        chatRoomData.setRoomName(roomName);
	        chatRoomData.setRoomType(PRIVATE_ROOM_TYPE);
	        chatRoomData.setTargetUserId(targetUserId.intValue());
	        chatRoomData.setMessages(chatService.readPrivateMessages(userId, chatId));
	    }

	    chatRoomData.getMessages().forEach(msg -> {
	        msg.setMyMessage(msg.getSenderId().equals(userId));
	        msg.setFormattedCreatedAt(msg.getCreatedAt() != null ?
	                DATE_FORMATTER.format(msg.getCreatedAt()) : "");
	    });
	    return chatRoomData;
	}

	private String handleSessionAction(HttpSession session, RedirectAttributes redirectAttributes, Runnable action) {
		Integer userId = validateSession(session, redirectAttributes);
		if (userId == null) {
			return LOGIN_REDIRECT;
		}
		try {
			action.run();
			return "redirect:/chat/main";
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/chat/main";
		}
	}

	private void addChatToSession(HttpSession session, Integer chatId) {
		List<Integer> openChats = getOpenChats(session);
		if (!openChats.contains(chatId)) {
			openChats.add(chatId);
			session.setAttribute("openChats", openChats);
		}
	}

	private void removeChatFromSession(HttpSession session, Integer chatId) {
		List<Integer> openChats = getOpenChats(session);
		openChats.remove(chatId);
		session.setAttribute("openChats", openChats);
	}

	@SuppressWarnings("unchecked")
	private List<Integer> getOpenChats(HttpSession session) {
		List<Integer> openChats = (List<Integer>) session.getAttribute("openChats");
		if (openChats == null) {
			openChats = new ArrayList<>();
			session.setAttribute("openChats", openChats);
		}
		return openChats;
	}

	private Integer validateSession(HttpSession session, RedirectAttributes redirectAttributes) {
		Integer userId = (Integer) session.getAttribute("userId");
		if (userId == null) {
			if (redirectAttributes != null) {
				redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
			}
			return null;
		}
		return userId;
	}

	private Integer getUserIdFromSession(HttpSession session) {
		Integer userId = (Integer) session.getAttribute("userId");
		if (userId == null) {
			throw new IllegalStateException("로그인된 사용자가 없습니다.");
		}
		return userId;
	}

	private void validateGroupName(String groupName) {
		if (groupName == null || groupName.trim().isEmpty()) {
			throw new IllegalArgumentException(GROUP_NAME_REQUIRED);
		}
	}

	private void validateSelfChat(HttpSession session, Integer targetUserId) {
		Integer userId = getUserIdFromSession(session);
		if (Objects.equals(userId, targetUserId)) {
			throw new IllegalArgumentException(SELF_CHAT_ERROR);
		}
	}

	private void validateSender(HttpSession session, Integer senderId) {
		Integer userId = getUserIdFromSession(session);
		if (!userId.equals(senderId)) {
			throw new IllegalArgumentException(AUTH_ERROR_MESSAGE);
		}
	}

	private ChatLogDto createChatLogDto(MultipartFile file, Integer roomId,
			String roomType, Integer senderId,
			String senderName, String fileUrl) {
		ChatLogDto dto = new ChatLogDto();
		dto.setSenderId(senderId);
		dto.setSenderName(senderName);
		dto.setMessage(file.getOriginalFilename());
		dto.setFile(fileUrl);
		dto.setFileType(file.getContentType());
		dto.setRoomId(roomId);
		dto.setRoomType(roomType);
		dto.setGroupId(roomId); // groupId를 roomId와 동일하게 설정
		dto.setCreatedAt(new Timestamp(System.currentTimeMillis()));
		dto.setMyMessage(true);
		return dto;
	}

	private void sendMessageViaWebSocket(ChatLogDto chatLogDto, Integer roomId, String roomType) {
		String destination = roomType.equals(GROUP_ROOM_TYPE) ?
				"/topic/group/" + roomId : "/user/queue/private";
		messagingTemplate.convertAndSend(destination, chatLogDto);
	}
}