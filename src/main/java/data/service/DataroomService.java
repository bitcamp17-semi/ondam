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
            String filename = file.getOriginalFilename();
            String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
            if (!List.of("pdf", "docx", "xlsx", "png", "jpg").contains(ext)) {
                throw new IllegalArgumentException("허용되지 않은 확장자입니다.");
            }

            String uploadedFilename = objectStorageService.uploadFile(bucketName, directoryPath, file);
            if (uploadedFilename == null) {
                throw new RuntimeException("네이버 클라우드에 파일 업로드 실패");
            }

            FilesDto fileRecord = new FilesDto();
            fileRecord.setTitle(title);
            fileRecord.setComment(description);
            fileRecord.setRoomId(roomId);
            fileRecord.setName(file.getOriginalFilename());
            fileRecord.setPath(uploadedFilename);
            fileRecord.setAuthorId(1); // TODO: 로그인 사용자로 변경 필요
            fileRecord.setType(file.getContentType());

            dataroomMapper.insertFile(fileRecord); // ✅ 핵심 저장 코드 추가

            System.out.println("파일 업로드 및 DB 저장 성공: " + uploadedFilename);
        } catch (Exception e) {
            System.err.println("에러 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("업로드 실패", e);
        }
    }
}