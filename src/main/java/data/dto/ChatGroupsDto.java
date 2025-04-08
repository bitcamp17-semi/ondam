package data.dto;

import lombok.Data;

import java.sql.Timestamp;

import org.apache.ibatis.type.Alias;

@Data
@Alias("ChatGroupsDto")
public class ChatGroupsDto {
    private int id;
    private String name;
    private Timestamp createdAt;
}
