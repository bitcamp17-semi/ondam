package data.dto;

import lombok.Data;

import java.sql.Timestamp;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Alias("AlarmDto")
public class AlarmDto {
    public enum AlarmType {
        MESSAGE, SCHEDULE, BOARD, APPROVAL, SYSTEM
    }
    private int id;
    private AlarmType type; //타입
    private boolean isRead; //조회여부 확인용
    private int userId; //알람받는 userId
    private int causedBy; //발생 id
    private String content; //알람 내용
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private Timestamp createdAt;
}
