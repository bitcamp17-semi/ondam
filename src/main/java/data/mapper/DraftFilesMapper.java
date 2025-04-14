package data.mapper;

import data.dto.DraftFilesDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DraftFilesMapper {
    public void createFiles(DraftFilesDto draftFilesDto);

    public List<DraftFilesDto> readFilesByDraft(int draftId);

    public DraftFilesDto readFileById(int fileId);
}
