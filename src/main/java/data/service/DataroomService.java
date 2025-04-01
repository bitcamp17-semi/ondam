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

    /**
     * (Read) 자료실ID와 검색어로 파일 목록 조회 기능
     *
     * @param roomId  카테고리 ID (0 = 전체)
     * @param keyword 검색어
     * @param offset  페이징 시작 위치
     * @param limit   한 페이지에 가져올 데이터 개수
     * @return 파일 목록
     */
    public List<FilesDto> readDataroomFilesByIdAndKeyword(int roomId, String keyword, int offset, int limit) {
        List<FilesDto> files = dataroomMapper.readDataroomFilesByIdAndKeyword(roomId, keyword, offset, limit);
        return files != null ? files : new ArrayList<>();
    }

    //  (Read) 자료실 모든 카테고리 목록 조회 기능
    public List<DataRoomDto> readDataroomCategories() {
        return dataroomMapper.readDataroomCategories();
    }

    // (Read) 파일 한 개 상세 조회 기능
    public FilesDto readDataroomById(int id) {
        FilesDto file = dataroomMapper.readDataroomById(id);
        if (file == null) {
            throw new IllegalArgumentException("해당 파일이 존재하지 않습니다. ID: " + id);
        }
        return file;
    }

    // (Create) 파일 신규 생성 기능
    public void createDataroomFile(FilesDto filesDto) {

        //db저장
        dataroomMapper.createDataroomFile(filesDto);
    }

    @Transactional
    public void deleteFileAndCloud(int id) {
        // 1. DB에서 파일 정보 조회
        FilesDto file = dataroomMapper.readDataroomById(id);
        if (file == null) {
            throw new IllegalArgumentException("삭제할 파일이 없습니다. ID: " + id);
        }

        // 2. NCloud (Object Storage)에서 파일 삭제
        // file.getPath()에 저장된 경로를 이용하여 삭제
        objectStorageService.deleteFile(objectStorageService.getBucketName(), "dataroom", file.getPath());

        // 3. DB에서 파일 삭제
        dataroomMapper.deleteDataroomById(id);
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
            fileRecord.setPath(uploadedFilename);                //Ncloud 에 랜덤으로 저장된 값
            fileRecord.setAuthorId(1);                           // 작성자 ID (로그인 구현 시 변경 필요)
            fileRecord.setType(file.getContentType());           // MIME 타입

            // DB 저장
            dataroomMapper.createDataroomFile(fileRecord);

            System.out.println("파일 업로드 및 DB 저장 성공: " + uploadedFilename);

        } catch (Exception e) {
            System.err.println("에러 발생: " + e.getMessage());
            e.printStackTrace(); // 전체 예외 출력
        }
    }
}