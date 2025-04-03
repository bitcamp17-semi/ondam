package data.controller;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import data.dto.ScheGroupMemberInserDto;
import data.dto.ScheduleGroupDto;
import data.dto.ScheduleGroupInsertDto;
import data.dto.ScheduleGroupMembersDto;
import data.dto.SchedulesDto;
import data.service.ScheduleGroupMembersService;
import data.service.ScheduleGroupService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ScheduleGroupController {
	final ScheduleGroupService scheduleGroupService;
	final ScheduleGroupMembersService scheduleGroupMemberService;
	
	//그룹 등록
	@PostMapping("/scheGroupInsert")
	@ResponseBody
	public ResponseEntity<Object> scheduleInsert(
			@RequestBody ScheduleGroupInsertDto body
			)
	{
		Map<String, Object> response = new LinkedHashMap<>();
	    try {
	        // 그룹 정보 삽입
	        Map<String, Object> groupMap = new HashMap<>();
	        groupMap.put("name", body.getName());
	        groupMap.put("departmentId", body.getDepartmentId());
	        groupMap.put("ownerId", body.getOwnerId());
	        groupMap.put("color", body.getColor());

	        //scheduleGroupService.scheGroupInsert(groupMap);
	       scheduleGroupService.scheGroupInsert(groupMap); // 리턴값 받아옴
	       //Integer groupId = (Integer) groupMap.get("id");
	       BigInteger bigId = (BigInteger) groupMap.get("id");
	       int groupId = bigId.intValue();
	       //int groupId = (Integer) groupMap.get("id"); // 생성된 그룹 ID
	        if (groupId == 0) {
                throw new IllegalStateException("그룹 생성 실패: groupId 반환되지 않음");
            }
	        
	        // 멤버 리스트 변환 후 삽입
	        List<Map<String, Object>> memberList = new ArrayList<>();
	        for (ScheGroupMemberInserDto member : body.getMembers()) {
	            Map<String, Object> memberMap = new HashMap<>();
	            memberMap.put("userId", member.getUserId());
	            memberMap.put("groupId", groupId); // 외래키 설정
	            memberMap.put("color", member.getColor());
	            memberList.add(memberMap);
	        }

	        scheduleGroupMemberService.scheGroupMemberInsert(memberList);
	        //return "일정 등록 완료";
	        
	        response.put("status", "ok");
            response.put("result", groupId);
            return new ResponseEntity<>(response, HttpStatus.OK);
	    } catch (Exception e) {
	    	e.printStackTrace(); // ← 콘솔에 전체 스택 출력
	        response.put("status", "error");
	        response.put("result", e.getMessage());
	    	response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
}
