package data.service;

import data.dto.DraftFilesDto;
import data.mapper.DraftFilesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DraftFilesService {
    @Autowired
    DraftFilesMapper draftFilesMapper;

    public void createFiles(DraftFilesDto draftFilesDto) {
        draftFilesMapper.createFiles(draftFilesDto);
    }
}
