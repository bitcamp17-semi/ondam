package data.dto;

import java.util.List;

import lombok.Data;

@Data
public class ScheduleGroupInsertDto {
	//그룹 등록 시 멤버도 같이 등록하기위해서 컨트롤러에서 호출하는 용도
	private String ownerId;
    private String name;
    private String color;
    private Integer departmentId;
    private List<ScheGroupMemberInserDto> members;
}
