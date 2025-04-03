package data.dto;

import java.sql.Timestamp;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Alias("ScheduleGroupDto")
@Data
public class ScheduleGroupDto {
	private int id;
	private String name;
	private Integer departmentId;
	private int ownerId;
	private String color;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private Timestamp createdAt;
	
}
