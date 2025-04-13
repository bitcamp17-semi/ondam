package data.mapper;

import data.dto.DraftFilesDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DraftFilesMapper {
    public void createFiles(DraftFilesDto draftFilesDto);
}
