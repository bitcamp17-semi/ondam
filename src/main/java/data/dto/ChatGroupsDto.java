package data.dto;

import lombok.Data;
import org.apache.ibatis.type.Alias;
import java.sql.Timestamp;

@Data
@Alias("ChatGroupsDto")
public class ChatGroupsDto {
    private Integer id;
    private String name;
    private Timestamp createdAt;
    private Long createdBy;
    private String lastMessage;
    private Integer memberCount;
    private String roomType;
    private String opponentName;
    private Long receiverId;
    private Long targetUserId;
    private String displayName;    
    private String displayInitial;
}