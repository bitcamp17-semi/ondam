package data.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import data.mapper.ScheduleGroupMembersMapper;

@Service
public class ScheduleGroupMembersService {
	@Autowired
	ScheduleGroupMembersMapper scheduleGroupMemberMapper;
	
	//등록(여러명 등록이 가능하도록 처리하기)
	public void scheGroupMemberInsert(List<Map<String, Object>> memberList) {
		Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("memberList", memberList);

        scheduleGroupMemberMapper.scheGroupMemberInsert(paramMap);
	}
}
