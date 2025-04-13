package data.service;

import data.dto.DataRoomDto;
import data.dto.FilesDto;
import data.mapper.DataroomMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class DataroomService {
    private final DataroomMapper dataroomMapper;
    private final ObjectStorageService objectStorageService;

    public DataroomService(DataroomMapper dataroomMapper, ObjectStorageService objectStorageService) {
        this.dataroomMapper = dataroomMapper;
        this.objectStorageService = objectStorageService;
    }

    // 폴더 목록 조회 (상위 폴더)
    public List<DataRoomDto> getAllFolders() {
        return dataroomMapper.readAllFolders();
    }

    // 자식 폴더 조회
    public List<DataRoomDto> getSubFolders(int parentId) {
        return dataroomMapper.readSubFolders(parentId);
    }

    // 폴더 이름 중복 체크
    public boolean checkFolderExists(String folderName, int parentId) {
        return dataroomMapper.readFolderExists(folderName, parentId);
    }

    // 폴더 추가
    public void addFolder(DataRoomDto folder) {
        dataroomMapper.insertFolder(folder);
    }

    // 폴더 삭제
    public void deleteFolder(int folderId) {
        dataroomMapper.deleteFolder(folderId);
    }

    public boolean hasChild(int folderId) {
        return dataroomMapper.readHasChild(folderId);
    }

    public List<FilesDto> getFilesByRoomId(int roomId) {
        return dataroomMapper.readFilesByRoomId(roomId);
    }



    @Transactional
    public void uploadFileAndSaveToDB(String bucketName, String directoryPath, MultipartFile file,
                                      String title, String description, Integer roomId) {
        try {
            // 1. 파일을 ncloud에 업로드
            String uploadedFilename = objectStorageService.uploadFile(bucketName, directoryPath, file);
            if (uploadedFilename == null) {
                throw new RuntimeException("네이버 클라우드에 파일 업로드 실패");
            }

            // 2. 업로드된 파일 정보를 DB에 저장
            FilesDto fileRecord = new FilesDto();
            fileRecord.setTitle(title);                          // 제목
            fileRecord.setComment(description);                  // 설명
            fileRecord.setRoomId(roomId);                        // 카테고리 ID
            fileRecord.setName(file.getOriginalFilename());      // 업로드된 파일명
            fileRecord.setPath(uploadedFilename);                // Ncloud에 랜덤으로 저장된 값
            fileRecord.setAuthorId(1);                           // 작성자 ID (로그인 구현 시 변경 필요)
            fileRecord.setType(file.getContentType());           // MIME 타입

            // DB 저장
            /*dataroomMapper.insertFolder(fileRecord); // 여기를 수정한 것!*/

            System.out.println("파일 업로드 및 DB 저장 성공: " + uploadedFilename);

        } catch (Exception e) {
            System.err.println("에러 발생: " + e.getMessage());
            e.printStackTrace(); // 전체 예외 출력
        }
    }
}