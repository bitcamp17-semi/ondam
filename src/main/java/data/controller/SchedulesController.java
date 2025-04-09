package data.controller;

import java.io.Console;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.util.JSONPObject;

import data.dto.ScheduleGroupDto;
import data.dto.ScheduleGroupMembersDto;
import data.dto.SchedulesDto;
import data.dto.UsersDto;
import data.mapper.ScheduleGroupMapper;
import data.service.AlarmService;
import data.service.ScheduleGroupMembersService;
import data.service.ScheduleGroupService;
import data.service.SchedulesService;
import data.service.UsersService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SchedulesController {
	final SchedulesService schedulesService;
	final UsersService userService;
	final ScheduleGroupService scheduleGroupService;
	final ScheduleGroupMembersService scheduleGroupMemberService;
	final AlarmService alarmService;
	
	//일정관리 페이지 진입
	@GetMapping({"/schedules"})
	public String scheduleMain(Model model, HttpSession session) {
		//int userId=1;//임시로 로그인한 사용자를 고정
		
		//세션에 저장된 userId 받기
		Integer sUserId=(Integer)session.getAttribute("userId");
		//System.out.println("로그인한 userId확인"+sUserId);
		
		if (sUserId == null) {
		    return "redirect:/login";
		}
		
		//아이디를 통해서 유저 테이블의 작성자 얻기
		String writer=userService.readUserById(sUserId).getName();
		
		//그룹장이름
		String ownerName=userService.readUserById(sUserId).getName();
		
		//로그인 시 로그인한 계정이 그룹장이며 그룹이름이 '개인일정'인 그룹
		//있는지 체크 후 없으면 그룹 자동 생성
		ScheduleGroupDto privateExisting=scheduleGroupService.readPrivateGroup(sUserId);
		if (privateExisting == null) {
			Map<String, Object> map = new HashMap<>();
	        map.put("name", "개인일정");
	        map.put("color", "#28a745"); // 초록계열
	        map.put("ownerId", sUserId);//dto.setOwnerId(userId);
	        //map.put("departmentId", "");//dto.setDepartmentId(null);

	        scheduleGroupService.scheGroupInsert(map);
	    }
		
		//로그한 계정이 '회사그룹'의 멤버로 없다면 멤버로 등록 시키기
		Integer companyMemExist=scheduleGroupService.readCompanyGroupMember(sUserId);
		if(companyMemExist==null)
		{
			//'회사그룹'의 그룹 id 저장
			Integer groupId=scheduleGroupService.readCompanyGroupId();
			
			if (groupId != null) { // 예외 대비해서 한 번 더 체크
				Map<String, Object> memberMap = new HashMap<>();
				memberMap.put("userId",sUserId);
				memberMap.put("groupId",groupId);
				memberMap.put("color","#ffa500");
			
				// Map 하나만 등록하더라도 리스트로 감싸서 넘기기 > scheGroupMemberInsert list를 반환하도록 되어있음
				List<Map<String, Object>> memberList = new ArrayList<>();
				memberList.add(memberMap);
				
				scheduleGroupMemberService.scheGroupMemberInsert(memberList);
				System.out.println("회사일정 그룹에 사용자 자동 추가 완료");
			}else {
				System.out.println("'회사일정' 그룹이 존재하지 않습니다.");
			}
		}
		
		//전체 일정 읽어오기
		List<SchedulesDto> list = schedulesService.readAllSche(sUserId);
		//전체 user 읽어오기
		List<UsersDto> userList=userService.readAllActiveUsers();
		//내가 그룹장이거나 그룹인원으로 있는 그룹 목록 불러오기
		List<ScheduleGroupDto> groupList=scheduleGroupService.readAllGroup(sUserId);
		for (ScheduleGroupDto group : groupList) {
		    int ownerId = group.getOwnerId();
		    String gownerName = userService.readUserById(ownerId).getName();
		    group.setOwnerName(gownerName); // ScheduleGroupDto에 ownerName 필드 필요
		}
		// 모든 그룹에 대해 그룹 멤버 조회 후 Map으로 담기
	    Map<Integer, List<Integer>> groupMemberMap = new HashMap<>();
	    for (ScheduleGroupDto group : groupList) {
	        List<ScheduleGroupMembersDto> members = scheduleGroupMemberService.readGroupMemByGroupId(group.getId());
	        List<Integer> memberIds = new ArrayList<>();
	        for (ScheduleGroupMembersDto mem : members) {
	            memberIds.add(mem.getUserId());
	        }
	        groupMemberMap.put(group.getId(), memberIds);
	    }
		
		model.addAttribute("userId",sUserId);
	    model.addAttribute("scheduleList", list); // 일정 리스트 모델에 담기
	    model.addAttribute("userList",userList); //사용자 목록 모델에 담기
	    model.addAttribute("groupList",groupList); //그룹목록 모델에 담기
	    model.addAttribute("groupMemberMap", groupMemberMap); // 멤버 ID 목록 map 추가
	    model.addAttribute("ownerName",ownerName);//로그인한 사용자가 가진 name을 ownerName으로 프론트에서 불러오기
	    model.addAttribute("today",new Date());
	    
	    return "schedules/schedules"; // schedules.html 읽어오기
	}
	
	//일정등록
	@PostMapping("/scheduleinsert")
	@ResponseBody
	public ResponseEntity<Object> scheduleInsert(
			@RequestBody SchedulesDto dto,
			HttpSession session
			)
	{
		Map<String, Object> response = new LinkedHashMap<>();
		
		//세션에 저장된 userId 받기
		int sUserId=(Integer)session.getAttribute("userId");
		//세선에 저장된 userId의 name을 writer에 저장
		String writer=userService.readUserById(sUserId).getName();
		
		try {
			String startTimeStr = dto.getStartDate() + " " + dto.getStartTime();
	        String endTimeStr = dto.getEndDate() + " " + dto.getEndTime();

	        // 초가 포함되어 있지 않으면 ":00" 추가
	        if (!startTimeStr.matches(".*:\\d{2}:\\d{2}$")) {
	            startTimeStr += ":00";
	        }
	        if (!endTimeStr.matches(".*:\\d{2}:\\d{2}$")) {
	            endTimeStr += ":00";
	        }

	        Timestamp startTimestamp = Timestamp.valueOf(startTimeStr);
	        Timestamp endTimestamp = Timestamp.valueOf(endTimeStr);

	        Map<String, Object> map = new HashMap<>();
	        map.put("userId", dto.getUserId());
	        map.put("name", dto.getName());
	        map.put("writer", writer);
	        map.put("content", dto.getContent());
	        map.put("groupId", dto.getGroupId());
	        map.put("isAlltime", dto.getIsAlltime());
	        map.put("startTime", startTimestamp);
	        map.put("endTime", endTimestamp);
	        map.put("startDate", dto.getStartDate());
	        map.put("endDate", dto.getEndDate());

	        schedulesService.scheduleInsert(map);
	        //return "일정 등록 완료";
	        
	        // 일정 등록 후 SSE 알림 전송
	        //alarmService.sendScheduleNotification((long) sUserId, dto.getName());
	        
	        response.put("status", "ok");
            response.put("result", map);
            return new ResponseEntity<>(response, HttpStatus.OK);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	//일정 상세
	@GetMapping("/scheDetail")
	public String Detail(@RequestParam(value="id") int id,Model model,HttpSession session)
	{
		//int userId=1;//임시로 로그인한 사용자를 고정
		//세션에 저장된 userId 받기
		int sUserId=(Integer)session.getAttribute("userId");
		//System.out.println("로그인한 userId확인"+userId);
		
		SchedulesDto dto=schedulesService.readOneSche(id);
		
		//System.out.println("isAlltime from DB: " + dto.getIsAlltime());
		
		//시작 날짜 및 시간 분리
		String[] startDateParts = dto.getStartTime().split(" ");
	    String startDate = startDateParts[0]; //2025-03-31 시작날짜
		String startTime = startDateParts[1];//15:52 시작 시간
	    
		//종료 날짜 및 시간 분리
		String[] endDateParts = dto.getEndTime().split(" ");
		String endDate=endDateParts[0];
		String endTime=endDateParts[1];
	    
		//내가 그룹장이거나 그룹인원으로 있는 그룹 목록 불러오기
		List<ScheduleGroupDto> groupList=scheduleGroupService.readAllGroup(sUserId);
		String groupName = dto.getGroupName();
		
		//작성자가 가진userId
		dto.setWriterId(dto.getUserId());
		
		model.addAttribute("dto",dto);
		model.addAttribute("userId",sUserId);//로그인한 사용자
		model.addAttribute("StartDate", startDate); //시작날짜
		model.addAttribute("StartTime",startTime); //시작 시간
		model.addAttribute("endDate",endDate);//마감 날짜
		model.addAttribute("endTime",endTime);//마감 시간
		model.addAttribute("groupList",groupList);//그룹목록
		model.addAttribute("groupName", groupName);//그룹명
		return "schedules/schedetail";
	}
	
	//일정 삭제
	@GetMapping("/scheDelete")
	public ResponseEntity<Void> deleteSchedules(@RequestParam(value="id") int id) {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		
		schedulesService.deleteSche(id);
	    return new ResponseEntity<>(HttpStatus.OK);
	}
	
	//일정 수정
	@PostMapping("/scheUpdate")
	public String scheduleUpdate(
			@ModelAttribute SchedulesDto dto
			)
	{
			//기존 저장된 날짜와 시간 불러오기
			String startDate=dto.getStartDate();
			String startTime=dto.getStartTime();
			String endDate=dto.getEndDate();
			String endTime=dto.getEndTime();
			
			String startTimeStr = startDate + " " + startTime;
	        String endTimeStr = endDate + " " + endTime;

	        // 초가 포함되어 있지 않으면 ":00" 추가
	        if (!startTimeStr.matches(".*:\\d{2}:\\d{2}$")) {
	            startTimeStr += ":00";
	        }
	        if (!endTimeStr.matches(".*:\\d{2}:\\d{2}$")) {
	            endTimeStr += ":00";
	        }
	        Timestamp startTimestamp = Timestamp.valueOf(startTimeStr);
	        Timestamp endTimestamp = Timestamp.valueOf(endTimeStr);
	        
	        // 필요시 dto에 다시 설정 가능
	        dto.setStartTime(startTimeStr);
	        dto.setEndTime(endTimeStr);
	        
			schedulesService.updateSchedule(dto);
			
			return "redirect:./scheDetail?id="+dto.getId();
	}
	  
}
