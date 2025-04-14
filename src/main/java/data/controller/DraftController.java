package data.controller;

import data.dto.*;
import data.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


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
    @Autowired
    DraftFilesService draftFilesService;
    @Autowired
    ObjectStorageService storageService;

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

    @PostMapping(value = "/createDraft", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createDraft(@RequestPart(value = "data") DraftFileReqDto data,
                                              @RequestPart(value = "uploads", required = false) MultipartFile[] uploads,
                                              HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        int userId = (Integer) session.getAttribute("userId");
        try {
            DraftsDto drafts = data.getDraft();
            drafts.setAuthorId(userId);
            draftService.createDraft(drafts);
            int draftId = drafts.getId(); // 생성된 기안문 id
            if (uploads != null && uploads.length > 0) {
                for (MultipartFile upload : uploads) {
                    if (upload != null && !upload.isEmpty() && !upload.getOriginalFilename().equals("")) {
                        String imageUrl = storageService.uploadFile(storageService.getBucketName(), "drafts", upload);
                        DraftFilesDto filesDto = new DraftFilesDto();
                        filesDto.setDraftId(draftId);
                        filesDto.setName(upload.getOriginalFilename());
                        filesDto.setPath(imageUrl); // storageUrl은 제외하고 생성된 파일명
                        draftFilesService.createFiles(filesDto);
                    }
                }
            }
            for (ApprovalsDto approval : data.getApprovals()) {
                approval.setDraftId(draftId);
                approval.setTemplateId(drafts.getTemplateId());
                approval.setStatus(ApprovalsDto.ApprovalStatus.PENDING);
                approvalsService.createApprovals(approval);
            }
            response.put("status", "ok");
            response.put("result", draftId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
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
            result.put("files", draftFilesService.readFilesByDraft(id));
            response.put("status", "ok");
            response.put("result", result);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/checkOrder")
    public ResponseEntity<Object> checkOrder(@RequestParam(value = "draftId") int draftId, HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        int userId = (Integer) session.getAttribute("userId");
        try {
            if (draftService.readCheckIsOrder(userId, draftId) == 1) {
                response.put("status", "ok");
                response.put("result", "check order successfully");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("status", "fail");
                response.put("result", "check order failed");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
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
            @RequestParam(value = "reason", required = false) String reason,
            HttpSession session
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        int userId = (Integer) session.getAttribute("userId");
        String actionUpperCase = action.toUpperCase();
        try {
            if (actionUpperCase.equals("APPROVED") || actionUpperCase.equals("REJECTED")) {
                approvalsService.updateApprovalsStatus(draftId, userId, action); // approvals 상태 변경
                draftService.stringToApprovalLogEnumAndCreateLog(draftId, userId, action, reason); // 승인 / 반려에 대해서만 로그 생성
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
    public ResponseEntity<Object> getPendingDrafts(
            HttpSession session,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Map<String, Object> result = new HashMap<>();
            Integer userId = (Integer) session.getAttribute("userId");
            int offset = (page - 1) * size;
            List<DraftsDto> drafts = draftService.getPendingDraftsForUser(userId, size, offset);
            Integer totalCnt = draftService.readCountDraftsForActions(userId);
            result.put("list", drafts);
            result.put("totalCnt", totalCnt);
            response.put("status", "ok");
            response.put("result", result);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("readSendDraftsById")
    public ResponseEntity<Object> readSendDraftsById(
            HttpSession session,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Map<String, Object> result = new HashMap<>();
            int userId = (Integer) session.getAttribute("userId");
            int offset = (page - 1) * size;
            List<DraftsDto> list = draftService.readPendingDraftsById(userId, size, offset);
            int totalCnt = draftService.readCountPendingdraftsById(userId);
            result.put("list", list);
            result.put("totalCnt", totalCnt);
            response.put("status", "ok");
            response.put("result", result);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/downloadAll/{draftId}")
    public ResponseEntity<Resource> downloadAll(@PathVariable int draftId) throws IOException {
        List<DraftFilesDto> files = draftFilesService.readFilesByDraft(draftId);
        if (files == null || files.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (files.size() < 4) {
            // 단일 파일 처리 (가장 첫 번째 파일만 다운로드)
            DraftFilesDto file = files.get(0);
            String fileUrl = "https://kr.object.ncloudstorage.com/bitcamp-semi/drafts/" + file.getPath();
            UrlResource resource = new UrlResource(fileUrl);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + URLEncoder.encode(file.getName(), StandardCharsets.UTF_8) + "\"")
                    .body(resource);
        }

        // ZIP 압축 처리 (4개 이상일 때)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(baos);

        for (DraftFilesDto file : files) {
            String fileUrl = "https://kr.object.ncloudstorage.com/bitcamp-semi/drafts/" + file.getPath();
            try (InputStream in = new URL(fileUrl).openStream()) {
                zipOut.putNextEntry(new ZipEntry(file.getName()));
                in.transferTo(zipOut);
                zipOut.closeEntry();
            }
        }
        zipOut.finish();

        ByteArrayResource zipResource = new ByteArrayResource(baos.toByteArray());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"draft_" + draftId + "_files.zip\"")
                .contentLength(zipResource.contentLength())
                .body(zipResource);
    }
    @GetMapping("/downloadFile/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable int fileId) throws MalformedURLException {
        DraftFilesDto file = draftFilesService.readFileById(fileId);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        String fileUrl = "https://kr.object.ncloudstorage.com/bitcamp-semi/drafts/" + file.getPath();
        UrlResource resource = new UrlResource(fileUrl);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + URLEncoder.encode(file.getName(), StandardCharsets.UTF_8) + "\"")
                .body(resource);
    }

    @GetMapping("/sentDoneList")
    public ResponseEntity<Object> sentDoneList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        int userId = (Integer) session.getAttribute("userId");
        try {
            Map<String, Object> result = new HashMap<>();
            int offset = (page - 1) * size;
            List<DraftsDto> list = draftService.readSentDoneById(userId, size, offset);
            int totalCnt = draftService.readCountSentDoneById(userId);
            result.put("list", list);
            result.put("totalCnt", totalCnt);
            response.put("status", "ok");
            response.put("result", result);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/receivedDoneList")
    public ResponseEntity<Object> receivedDoneList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            HttpSession session
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        int userId = (Integer) session.getAttribute("userId");
        try {
            Map<String, Object> result = new HashMap<>();
            int offset = (page - 1) * size;
            List<DraftsDto> list = draftService.readReceivedDoneById(userId, size, offset);
            int totalCnt = draftService.readCountReceivedDoneById(userId);
            result.put("list", list);
            result.put("totalCnt", totalCnt);
            response.put("status", "ok");
            response.put("result", result);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}