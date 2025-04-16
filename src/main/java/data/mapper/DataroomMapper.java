package data.mapper;

import data.dto.DataRoomDto;
import data.dto.FilesDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DataroomMapper {
    // 특정 자료실(roomId)의 모든 파일 목록 조회
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

    void deleteFiles(int id);

    FilesDto readDataroomById(int id);

    List<FilesDto> readFilesByDepartmentId(@Param("departmentId") int departmentId);

    // @Select 어노테이션을 사용하여 SQL을 직접 작성

    List<FilesDto> readFilesByTeamId(@Param("teamId") Integer teamId);

    String readUserNameById(@Param("id") int id);

    List<FilesDto> readFilesByIds(@Param("ids") List<Integer> ids);


    int readDepartmentOwnerId(@Param("id") int departmentId);

    String readTeamNameById(@Param("id") int teamId);

    String readTeamNameByFolderId(@Param("folderId") int folderId);

}
