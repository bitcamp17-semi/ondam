package data.mapper;

import data.dto.FilesDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
    public interface DataroomMapper {
    //특정 자료실(roomId)의 모든 파일 목록 조회
    List<FilesDto> readFilesByRoomId(@Param("roomId") int roomId,
                                     @Param("keyword") String keyword,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);

    //카테고리 얻기
    List<String> readCategories();

    //특정 ID로 파일 찾기
    FilesDto readById(@Param("id") int id);

    // 파일 저장
    void createFile(FilesDto file);

    // 파일 삭제
    void deleteById(@Param("id") int id);
}
