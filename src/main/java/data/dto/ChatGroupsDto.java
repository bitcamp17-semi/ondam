package data.dto;

import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDate;

import org.apache.ibatis.type.Alias;

@Data
@Alias("ChatGroupsDto")
public class ChatGroupsDto {
    private int id;
    private String name;
    private LocalDate createdAt;
    private int createdBy;
}
