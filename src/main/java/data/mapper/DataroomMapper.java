package data.mapper;

import data.dto.DataRoomDto;
import data.dto.FilesDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
    public interface DataroomMapper {
    //특정 자료실(roomId)의 모든 파일 목록 조회
    List<FilesDto> readDataroomFilesByIdAndKeyword(@Param("roomId") int roomId,
                                     @Param("keyword") String keyword,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);

    // 모든 자료실 카테고리 조회 (Read)
    public List<DataRoomDto> readDataroomCategories();

    // 특정 ID로 파일 한 개 조회 (Read)
    FilesDto readDataroomById(@Param("id") int id);

    // 파일 신규 생성
    void createDataroomFile(FilesDto file);

    // 특정 ID 파일 삭제 (Delete)
    void deleteDataroomById(@Param("id") int id);
}
