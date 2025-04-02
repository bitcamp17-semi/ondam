package data.controller;

import data.dto.DraftTemplatesDto;
import data.dto.DraftsDto;
import data.service.ApprovalsService;
import data.service.DraftService;
import data.service.UsersService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/draft")
public class DraftController {
    @Autowired
    DraftService draftService;
    @Autowired
    ApprovalsService approvalsService;
    @Autowired
    UsersService usersService;

    @GetMapping("/createTemplate")
    public ResponseEntity<Object> createTemplate(@ModelAttribute DraftTemplatesDto paramsDto, HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        int userId = Integer.parseInt((String) session.getAttribute("userId"));
        if (usersService.isAdmin(userId)) {
            try {
                draftService.createDraftTemplate(paramsDto);
                response.put("status", "ok");
                response.put("result", paramsDto);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } catch (Exception e) {
                response.put("status", "error");
                response.put("result", e.getMessage());
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            response.put("status", "fail");
            response.put("result", "you're not admin");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/readTemplate")
    public ResponseEntity<Object> readTemplate(@RequestParam int id) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("status", "ok");
            response.put("result", draftService.readDraftTemplate(id));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/readAllTemplate")
    public ResponseEntity<Object> readAllTemplate() {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("status", "ok");
            response.put("result", draftService.readAllDraftTemplate());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/updateTemplate")
    public ResponseEntity<Object> updateTemplate(@ModelAttribute DraftTemplatesDto paramsDto, HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        int userId = Integer.parseInt((String) session.getAttribute("userId"));
        if (usersService.isAdmin(userId)) {
            try {
                draftService.updateDraftTemplate(paramsDto);
                response.put("status", "ok");
                response.put("result", paramsDto);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } catch (Exception e) {
                response.put("status", "error");
                response.put("result", e.getMessage());
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            response.put("status", "fail");
            response.put("result", "you're not admin");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/deleteTemplate")
    public ResponseEntity<Object> deleteTemplate(@RequestParam int id, HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        int userId = Integer.parseInt((String) session.getAttribute("userId"));
        if (usersService.isAdmin(userId)) {
            try {
                draftService.deleteDraftTemplate(id);
                response.put("status", "ok");
                response.put("result", "delete successfully");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } catch (Exception e) {
                response.put("status", "error");
                response.put("result", e.getMessage());
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            response.put("status", "fail");
            response.put("result", "you're not admin");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/createDraft")
    public ResponseEntity<Object> createDraft(@ModelAttribute DraftsDto paramsDto, HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        int userId = Integer.parseInt((String) session.getAttribute("userId"));
        if (usersService.isAdmin(userId)) {
            try {
                draftService.createDraft(paramsDto);
                response.put("status", "ok");
                response.put("result", paramsDto);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } catch (Exception e) {
                response.put("status", "error");
                response.put("result", e.getMessage());
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            response.put("status", "fail");
            response.put("result", "you're not admin");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/readDraft")
    public ResponseEntity<Object> readDraft(@RequestParam int id) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("status", "ok");
            response.put("result", draftService.readDraft(id));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/readAllDraft")
    public ResponseEntity<Object> readAllDraft() {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("status", "ok");
            response.put("result", draftService.readAllDrafts());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/updateStatus")
    public ResponseEntity<Object> updateStatus(@RequestParam int id, @RequestParam String status) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            // todo : 상태에 따른 처리 및 알림 기능 추가 필요
            draftService.updateDraftStatus(id, status);
            response.put("status", "ok");
            response.put("result", "status change to '" + status + "' successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // todo : 미완성
    @GetMapping("/{draftId}/actions")
    public ResponseEntity<Object> actions(
            @PathVariable int draftId,
            @RequestParam String action,
            HttpSession session
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        int userId = Integer.parseInt((String) session.getAttribute("userId"));
        try {
            approvalsService.updateApprovalsStatus(draftId, userId, action);
            draftService.stringToApprovalLogEnumAndCreateLog(action, draftId, userId);
            if (approvalsService.readNextApprovalId(draftId, userId) == 0 ) {
                draftService.updateDraftStatus(draftId, action);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
