package data.mapper;

import data.dto.DraftTemplatesDto;
import data.dto.DraftsDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DraftMapper {
    public void createDraftTemplate(DraftTemplatesDto draftTemplatesDto);

    public DraftTemplatesDto readDraftTemplate(int id);

    public List<DraftTemplatesDto> readAllDraftTemplate();

    public void updateDraftTemplate(DraftTemplatesDto draftTemplatesDto);

    public void deleteDraftTemplate(int id);

    public void createDraft(DraftsDto draftsDto);

    public DraftsDto readDraft(int id);

    public List<DraftsDto> readAllDrafts();

    public void updateDraftStatus(int id, String status);

    public List<DraftsDto> readPendingDraftsForUser(int userId, int size, int offset);

    public Integer readCountDraftsForActions(int userId);

    public Integer readCheckIsOrder(int userId, int draftId);

    public List<DraftsDto> readPendingDraftsById(int userId, int size, int offset);

    public Integer readCountPendingdraftsById(int userId);
}
