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
}
