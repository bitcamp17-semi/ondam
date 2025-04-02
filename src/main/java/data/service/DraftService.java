package data.service;

import data.dto.ApprovalLogDto;
import data.dto.ApprovalsDto;
import data.dto.DraftTemplatesDto;
import data.dto.DraftsDto;
import data.mapper.DraftMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DraftService {
    @Autowired
    DraftMapper draftMapper;
    @Autowired
    ApprovalsService approvalsService;

    public void createDraftTemplate(DraftTemplatesDto draftTemplatesDto) {
        draftMapper.createDraftTemplate(draftTemplatesDto);
    };

    public DraftTemplatesDto readDraftTemplate(int id) {
        return draftMapper.readDraftTemplate(id);
    };

    public List<DraftTemplatesDto> readAllDraftTemplate() {
        return draftMapper.readAllDraftTemplate();
    }

    public void updateDraftTemplate(DraftTemplatesDto draftTemplatesDto) {
        draftMapper.updateDraftTemplate(draftTemplatesDto);
    };

    public void deleteDraftTemplate(int id) {
        draftMapper.deleteDraftTemplate(id);
    };

    public void createDraft(DraftsDto draftsDto) {
        draftMapper.createDraft(draftsDto);
    };

    public DraftsDto readDraft(int id) {
        return draftMapper.readDraft(id);
    };

    public List<DraftsDto> readAllDrafts() {
        return draftMapper.readAllDrafts();
    };

    public void updateDraftStatus(int id, String status) {
        draftMapper.updateDraftStatus(id, status);
    };

    public void stringToEnumAndUpdate(int id, String status) {
        ApprovalsDto.ApprovalStatus approvalStatus = ApprovalsDto.ApprovalStatus.valueOf(status);
    }

    public void stringToApprovalLogEnumAndCreateLog(String action, int draftId, int userId) {
        ApprovalLogDto.ApprovalAction actionEnum = ApprovalLogDto.ApprovalAction.valueOf(action.toUpperCase());
        ApprovalLogDto logDto = new ApprovalLogDto();
        logDto.setAction(actionEnum);
        logDto.setDraftId(draftId);
        logDto.setApprovalId(userId);
        approvalsService.createApprovalLog(logDto);
    }
}
