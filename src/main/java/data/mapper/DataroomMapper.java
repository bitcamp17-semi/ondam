package data.mapper;

import data.dto.DataRoomDto;
import data.dto.FilesDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
    public interface DataroomMapper {
    //특정 자료실(roomId)의 모든 파일 목록 조회
    // 폴더 목록 조회 (상위 폴더)
    List<DataRoomDto> readAllFolders();

    // 자식 폴더 조회
    List<DataRoomDto> readSubFolders(@Param("parentId") int parentId);

    // 폴더 이름 중복 체크
    boolean readFolderExists(@Param("folderName") String folderName, @Param("parentId") int parentId);

    // 폴더 추가
    void insertFolder(DataRoomDto folder);

    // 폴더 삭제
    void deleteFolder(@Param("folderId") int folderId);

    boolean readHasChild(@Param("folderId") int folderId);

    List<FilesDto> readFilesByRoomId(@Param("roomId") int roomId);

    void insertFile(FilesDto file);

    public void deleteFiles(int id);

    public FilesDto readDataroomById(int id);
}
