package data.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleGroupMembersMapper {
	//등록
	public void scheGroupMemberInsert(Map<String,Object> map);

}
