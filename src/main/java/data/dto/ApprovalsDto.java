package data.dto;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("ApprovalsDto")
public class ApprovalsDto {
    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED, CANCELED
    }
    private int id;
    private int userId;
    private Integer draftId;
    private Integer templateId;
    private int order;
    private ApprovalStatus status;
    // user 정보를 위한 필드
    private String userName;
    private String profileImage;
    private String position;
}
