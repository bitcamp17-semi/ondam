package data.dto;

import java.util.List;

import lombok.Data;

@Data
public class ScheduleGroupInsertDto {
	private String ownerId;
    private String name;
    private String color;
    private Integer departmentId;
    private List<ScheGroupMemberInserDto> members;
}
