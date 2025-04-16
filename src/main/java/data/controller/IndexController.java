package data.controller;

import data.dto.BoardDto;
import data.dto.SchedulesDto;
import data.mapper.IndexMapper;
import data.service.DraftService;
import data.service.SchedulesService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/index")
public class IndexController {
    @Autowired
    DraftService draftService;
    @Autowired
    IndexMapper indexMapper;
    @Autowired
    SchedulesService schedulesService;

    @GetMapping("/draftCnt")
    public ResponseEntity<Object> homeDraftCnt(HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> result = new HashMap<>();
        int userId = (Integer) session.getAttribute("userId");
        try {
            result.put("received",draftService.readCountDraftsForActions(userId));
            result.put("sent", draftService.readCountPendingdraftsById(userId));
            result.put("done", draftService.readCountSentDoneById(userId));
            response.put("status", "ok");
            response.put("result", result);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/noticeTop3")
    public ResponseEntity<Object> homeNoticeTop3() {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            List<BoardDto> list = indexMapper.readNoticeTop3();
            response.put("status", "ok");
            response.put("result", list);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/calendar-data")
    public ResponseEntity<Object> calendarData(HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                response.put("status", "unauthorized");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            List<SchedulesDto> scheduleList = schedulesService.readAllSche(userId);
            response.put("status", "ok");
            response.put("result", scheduleList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
