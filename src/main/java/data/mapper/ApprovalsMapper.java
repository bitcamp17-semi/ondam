package data.mapper;

import data.dto.ApprovalLogDto;
import data.dto.ApprovalsDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ApprovalsMapper {
    public void createApprovals(ApprovalsDto approvalsDto);

    public void updateApprovalsStatus(int draftId, int userId, String status);

    public void createApprovalLog(ApprovalLogDto approvalLogDto);

    public List<ApprovalsDto> readApprovalsByDraft(int draftId);

    public List<ApprovalsDto> readApprovalsByTemplate(int TemplateId);

    public List<ApprovalLogDto> readApprovalLogByDraft(int draftId);

    public Integer readNextApprovalId(int draftId, int userId);
}
