package data.controller;

import data.dto.*;
import data.service.ApprovalsService;
import data.service.DraftService;
import data.service.UsersService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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

    @PostMapping("/createTemplate")
    public ResponseEntity<Object> createTemplate(@RequestBody TemplateCreateReqDto request,
                                                 HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        int userId = (Integer) session.getAttribute("userId");
        if (usersService.isAdmin(userId)) {
            try {
                DraftTemplatesDto paramsDto = request.getTemplate();
                List<ApprovalsDto> approvalsList = request.getApprovals();
                paramsDto.setAuthorId(userId);
                draftService.createDraftTemplate(paramsDto);
                int templateId = paramsDto.getId();
                for (ApprovalsDto approvals : approvalsList) { // 템플릿의 결재자 지정
                    approvals.setTemplateId(templateId);
                    approvalsService.createApprovals(approvals);
                }
                Map<String, Object> result = new HashMap<>();
                result.put("template", paramsDto);
                result.put("approvals", approvalsList);
                response.put("status", "ok");
                response.put("result", result);
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
    public ResponseEntity<Object> readTemplate(@RequestParam(value = "id") int id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("template", draftService.readDraftTemplate(id));
            result.put("approvalList", approvalsService.readApprovalsByTemplate(id));
            response.put("status", "ok");
            response.put("result", result);
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

    @PostMapping("/updateTemplate")
    public ResponseEntity<Object> updateTemplate(@ModelAttribute DraftTemplatesDto paramsDto, HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        int userId = (Integer) session.getAttribute("userId");
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
    public ResponseEntity<Object> deleteTemplate(@RequestParam(value = "id") int id, HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        int userId = (Integer) session.getAttribute("userId");
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

    @PostMapping("/createDraft")
    public ResponseEntity<Object> createDraft(@ModelAttribute DraftsDto paramsDto, HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        int userId = (Integer) session.getAttribute("userId");
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
    public ResponseEntity<Object> readDraft(@RequestParam(value = "id") int id) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("draft", draftService.readDraft(id));
            result.put("approvals", approvalsService.readApprovalsByDraft(id));
            result.put("approvalsLog", approvalsService.readApprovalLogByDraft(id));
            response.put("status", "ok");
            response.put("result", result);
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
    public ResponseEntity<Object> updateStatus(@RequestParam(value = "id") int id, @RequestParam String status) {
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

    @GetMapping("/{draftId}/actions")
    public ResponseEntity<Object> actions(
            @PathVariable int draftId,
            @RequestParam(value = "action") String action,
            HttpSession session
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        int userId = (Integer) session.getAttribute("userId");
        String actionUpperCase = action.toUpperCase();
        try {
            if (actionUpperCase.equals("APPROVED") || actionUpperCase.equals("REJECTED")) {
                approvalsService.updateApprovalsStatus(draftId, userId, action); // approvals 상태 변경
                draftService.stringToApprovalLogEnumAndCreateLog(draftId, userId, action); // 승인 / 반려에 대해서만 로그 생성
            }
            int nextApprovalId = approvalsService.readNextApprovalId(draftId, userId);
            if (nextApprovalId == 0) { // 다음 결재자가 없을 경우
                draftService.updateDraftStatus(draftId, action); // 기안문 최종 상태 변경
                // todo : 기안자에게 알림 생성 로직
            } else {
                // todo : nextApproval에게 알림 생성 로직
            }
            response.put("status", "ok");
            response.put("result", "status change to '" + action + "' successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/actionRequired")
    public ResponseEntity<Object> getPendingDrafts(HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            List<DraftsDto> drafts = draftService.getPendingDraftsForUser(userId);
            response.put("status", "ok");
            response.put("result", drafts);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}