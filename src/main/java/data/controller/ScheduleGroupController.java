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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
	       
	       System.out.println("그룹 생성 후 반환된 groupId: " + groupMap.get("id"));
	       
	       // 디버깅 출력
	        System.out.println("groupMap keys: " + groupMap.keySet());
	        System.out.println("groupMap id: " + groupMap.get("id"));
	       
	        // ID 꺼내기 (null 체크 포함)
	        Object idObj = groupMap.get("id");
	        if (idObj == null) {
	            throw new IllegalStateException("그룹 생성 실패: 반환된 ID 없음");
	        }
	        
	       //위에서 그룹 아이디를 생성하고 그걸 받아서 그룹 멤버를 저장해야함
	       //근데 이게 그룹 아이디가 auto_increment로 지정되어 있어서 BigInteger로 형변환을 해주는게 안전함
	       //만약 bigInteger를 사용하지 않는 경우 지금은 오류 발생하면서 등록되지 않음
	       //Integer groupId = (Integer) groupMap.get("id");
	       BigInteger bigId = (BigInteger) groupMap.get("id");
	       int groupId = bigId.intValue();
	       //int groupId = (Integer) groupMap.get("id"); // 생성된 그룹 ID
	        if (groupId == 0) {
                throw new IllegalStateException("그룹 생성 실패: groupId 반환되지 않음");
            }
	        
	        // 멤버 리스트 변환 후 삽입
	        List<ScheGroupMemberInserDto> members = body.getMembers();
	        if (members != null && !members.isEmpty()) {
	            List<Map<String, Object>> memberList = new ArrayList<>();
	        for (ScheGroupMemberInserDto member : body.getMembers()) {
	            Map<String, Object> memberMap = new HashMap<>();
	            memberMap.put("userId", member.getUserId());
	            memberMap.put("groupId", groupId);
	            memberMap.put("color", member.getColor());
	            memberList.add(memberMap);
	        }
	        // mapper에서 memberList 받아야 하므로 Map에 담아서 넘기기
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("memberList", memberList);
	        
	        scheduleGroupMemberService.scheGroupMemberInsert(paramMap);
	        }
	        response.put("status", "ok");
            response.put("result", groupId);
            return new ResponseEntity<>(response, HttpStatus.OK);
	    } catch (Exception e) {
	    	e.printStackTrace(); // ← 콘솔에 전체 스택 출력
	    	response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	//그룹 수정
	@PostMapping("/updateScheGroup")
	@ResponseBody
	public ResponseEntity<Object> updateScheGroup(
			@RequestBody ScheduleGroupDto dto
			)
	{
		Map<String, Object> response = new LinkedHashMap<>();
	    try {
	        
	    	//기존에 저장된 데이터 불러오기
	    	String name=dto.getName();
	    	String color=dto.getColor();
	    	int groupId=dto.getId();
	    	
	    	// 그룹 정보 업데이트
	        scheduleGroupService.updateSchGroup(dto);
	        
	        //기존 멤버 삭제 (수정은 여러개를 한번에 수정 불가능해서 삭제 > 추가로 해야함)
	        scheduleGroupMemberService.deleteScheGroupMem(groupId);

	        //새로운 멤버 추가
	        List<Map<String, Object>> members = new ArrayList<>();
	        for (ScheduleGroupMembersDto m : dto.getMembers()) {
	            Map<String, Object> map = new HashMap<>();
	            map.put("userId", m.getUserId());
	            map.put("groupId", dto.getId());
	            map.put("color", m.getColor());
	            members.add(map);
	        }
	        
	        //멤버가 있을 때만 insert 실행
	        if (!members.isEmpty()) {
	        	Map<String, Object> paramMap = new HashMap<>();
	        	paramMap.put("memberList", members);
	        	scheduleGroupMemberService.scheGroupMemberInsert(paramMap);
	        }
	        
	        response.put("status", "ok");
            response.put("resuilt", dto);
            return new ResponseEntity<>(response, HttpStatus.OK);
	    } catch (Exception e) {
	    	e.printStackTrace(); //콘솔에 전체 스택 출력
	    	response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	//그룹 삭제
	@GetMapping("/scheGroupDel")
	public ResponseEntity<Void> deleteScheGroup(@RequestParam(value="id") int id) {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		
		scheduleGroupService.deleteScheGroup(id);
	    return new ResponseEntity<>(HttpStatus.OK);
	}
}
