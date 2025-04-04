package data.service;

import data.dto.ApprovalLogDto;
import data.dto.ApprovalsDto;
import data.mapper.ApprovalsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApprovalsService {
    @Autowired
    ApprovalsMapper approvalsMapper;

    public void createApprovals(ApprovalsDto approvalsDto) {
        approvalsMapper.createApprovals(approvalsDto);
    };

    public void updateApprovalsStatus(int draftId, int userId, String status) {
        approvalsMapper.updateApprovalsStatus(draftId, userId, status);
    };

    public void createApprovalLog(ApprovalLogDto approvalLogDto) {
        approvalsMapper.createApprovalLog(approvalLogDto);
    }

    public List<ApprovalsDto> readApprovalsByDraft(int draftId) {
        return approvalsMapper.readApprovalsByDraft(draftId);
    }

    public List<ApprovalsDto> readApprovalsByTemplate(int TemplateId) {
        return approvalsMapper.readApprovalsByTemplate(TemplateId);
    };

    public List<ApprovalLogDto> readApprovalLogByDraft(int draftId) {
        return approvalsMapper.readApprovalLogByDraft(draftId);
    };

    public int readNextApprovalId(int draftId, int userId) {
        Integer nextApprover = approvalsMapper.readNextApprovalId(draftId, userId);
        if (nextApprover == null) {
            return 0;
        } else {
            return nextApprover;
        }
    }
}
