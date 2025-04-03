package data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.sql.Timestamp;
@Alias("SchedulesDto")
@Data
public class SchedulesDto {
    private int id;
    private String name;
    private int userId;
    private String content;
    private int isAlltime;
    
    private String startTime; //HH:mm
    private String endTime;//HH:mm
    private String startDate;//yyyy-MM-dd
    private String endDate;//yyyy-MM-dd
    private int groupId;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Timestamp createdAt;
    //작성자
    private String writer;
}
