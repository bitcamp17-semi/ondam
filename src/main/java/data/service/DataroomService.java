package data.service;

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

    /**
     * 특정 자료실(roomId)의 파일 목록 조회 (페이징 적용)
     *
     * @param roomId  카테고리 ID (0 = 전체)
     * @param keyword 검색어
     * @param offset  페이징 시작 위치
     * @param limit   한 페이지에 가져올 데이터 개수
     * @return 파일 목록
     */
    public List<FilesDto> readFilesByRoomId(int roomId, String keyword, int offset, int limit) {
        List<FilesDto> files = dataroomMapper.readFilesByRoomId(roomId, keyword, offset, limit);
        return files != null ? files : new ArrayList<>();
    }

    // 자료실 목록 가져오기 (카테고리)
    public List<String> readCategories() {
        return dataroomMapper.readCategories();
    }

    // 특정 파일 ID로 찾기
    public FilesDto readById(int id) {
        FilesDto file = dataroomMapper.readById(id);
        if (file == null) {
            throw new IllegalArgumentException("해당 파일이 존재하지 않습니다. ID: " + id);
        }
        return file;
    }

    // 파일 저장
    public void createFile(FilesDto filesDto) {

        //db저장
        dataroomMapper.createFile(filesDto);
    }

    // 파일 삭제
    public void deleteFile(int id) {
        FilesDto file = dataroomMapper.readById(id);
        if (file == null) {
            throw new IllegalArgumentException("삭제할 파일이 없습니다. ID: " + id);
        }
        dataroomMapper.deleteById(id);
    }

    public String uploadFileToCloud(String bucketName, String directoryPath, MultipartFile file) {
        System.out.println("Uploading file to bucket: " + bucketName);
        System.out.println("Uploading to directory: " + directoryPath);
        System.out.println("Original filename: " + file.getOriginalFilename());
        String uploadedFilename = objectStorageService.uploadFile(
                objectStorageService.getBucketName(),
                directoryPath,
                file
        );

        return uploadedFilename; // 성공 시 업로드된 파일 이름 반환
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
            fileRecord.setName(uploadedFilename);                // 업로드된 파일명
            fileRecord.setPath(directoryPath);                   // 업로드 경로
            fileRecord.setAuthorId(1);                           // 작성자 ID (로그인 구현 시 변경 필요)
            fileRecord.setType(file.getContentType());           // MIME 타입

            // DB 저장
            dataroomMapper.createFile(fileRecord);

            System.out.println("파일 업로드 및 DB 저장 성공: " + uploadedFilename);

        } catch (Exception e) {
            System.err.println("에러 발생: " + e.getMessage());
            e.printStackTrace(); // 전체 예외 출력
        }
    }
}