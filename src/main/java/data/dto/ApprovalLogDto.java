package data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.sql.Timestamp;

@Data
@Alias("ApprovalLogDto")
public class ApprovalLogDto {
    public enum ApprovalAction{
        APPROVED, REJECTED
    }
    private int id;
    private int approvalId;
    private int draftId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private Timestamp approvalTime;
    private ApprovalAction action;
    private int order; // approvals 테이블과의 조인 결과를 담기 위한 field 추가
}
