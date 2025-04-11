package data.service;

import data.dto.DraftFilesDto;
import data.mapper.DraftFilesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DraftFilesService {
    @Autowired
    DraftFilesMapper draftFilesMapper;

    public void createFiles(DraftFilesDto draftFilesDto) {
        draftFilesMapper.createFiles(draftFilesDto);
    }

    public List<DraftFilesDto> readFilesByDraft(int draftId) {
        return draftFilesMapper.readFilesByDraft(draftId);
    }

    public DraftFilesDto readFileById(int fileId) {
        return draftFilesMapper.readFileById(fileId);
    }
}
