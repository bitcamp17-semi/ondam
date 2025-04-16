package data.service;

import data.dto.DataRoomDto;
import data.dto.FilesDto;
import data.dto.UsersDto;
import data.mapper.DataroomMapper;
import data.mapper.UsersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import util.ZipUtility;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class DataroomService {
    private final DataroomMapper dataroomMapper;
    private final ObjectStorageService objectStorageService;
    private final UsersMapper usersMapper;
    private final ZipUtility zipUtility;



    public DataroomService(DataroomMapper dataroomMapper, ObjectStorageService objectStorageService, UsersMapper usersMapper, ZipUtility zipUtility) {
        this.dataroomMapper = dataroomMapper;
        this.objectStorageService = objectStorageService;
        this.usersMapper = usersMapper;
        this.zipUtility = zipUtility;
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
                                      String title, String category, Integer departmentId, Integer teamId, int userId) {
        try {
            System.out.println("파일 업로드 시작: " + file.getOriginalFilename());
            String filename = file.getOriginalFilename();
            String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

            if (!List.of("pdf", "docx", "xlsx", "png", "jpg","doc","ppt","pptx").contains(ext)) {
                throw new IllegalArgumentException("허용되지 않은 확장자입니다.");
            }

            String uploadedFilename = objectStorageService.uploadFile(bucketName, directoryPath, file);
            if (uploadedFilename == null) {
                throw new RuntimeException("네이버 클라우드에 파일 업로드 실패");
            }

            FilesDto fileRecord = new FilesDto();
            fileRecord.setTitle(title);
            fileRecord.setCategory(category);
            fileRecord.setDepartmentId(departmentId);
            fileRecord.setTeamId(teamId);
            fileRecord.setName(file.getOriginalFilename());
            fileRecord.setPath(uploadedFilename);
            fileRecord.setAuthorId(userId);
            fileRecord.setType(file.getContentType());

            // **디버깅: 파일 정보 출력**
            System.out.println("파일 저장 정보: " + fileRecord);

            // DB에 저장
            dataroomMapper.insertFile(fileRecord);
            System.out.println("파일 DB 저장 성공: " + uploadedFilename);
        } catch (Exception e) {
            System.err.println("에러 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("업로드 실패", e);
        }
    }


    public List<Map<String, Object>> getFilesWithAuthorName(Integer departmentId, Integer teamId) {
        List<FilesDto> files = getFilesByFolder(departmentId, teamId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (FilesDto file : files) {
            Map<String, Object> fileMap = new HashMap<>();
            fileMap.put("id", file.getId());
            fileMap.put("name", file.getName());
            fileMap.put("title", file.getTitle());
            fileMap.put("category", file.getCategory());
            fileMap.put("path", file.getPath());
            fileMap.put("createdAt", file.getCreatedAt());
            fileMap.put("updatedAt", file.getUpdatedAt());
            fileMap.put("authorId", file.getAuthorId());
            fileMap.put("type", file.getType());
            fileMap.put("departmentId", file.getDepartmentId());
            fileMap.put("teamId", file.getTeamId());

            // authorName 추가
            String authorName = dataroomMapper.readUserNameById(file.getAuthorId());
            fileMap.put("authorName", authorName);

            result.add(fileMap);
        }

        return result;
    }

    public String createZipAndGetUrl(List<Integer> ids) throws IOException {
        List<FilesDto> files = dataroomMapper.readFilesByIds(ids);
        System.out.println("압축할 파일 개수: " + files.size());
        for (FilesDto file : files) {
            System.out.println("파일 path: " + file.getPath() + ", name: " + file.getName());
        }
        if (files == null || files.isEmpty()) {
            throw new RuntimeException("선택된 파일이 없습니다.");
        }

        System.out.println("파일 목록: " + files.size() + "개 파일 반환");

        String zipFileName = "download_" + System.currentTimeMillis() + ".zip";
        try {
            File zipFile = zipUtility.createZip(files, zipFileName); // 유틸에서 zip 생성
            return objectStorageService.uploadTempZipAndGetUrl(zipFile);
        } catch (Exception e) {
            e.printStackTrace(); // 예외 출력
            throw new RuntimeException("파일 다운로드 처리 중 오류가 발생했습니다.", e);
        }
    }



    public void deleteFiles(int id) {
        dataroomMapper.deleteFiles(id);
    }

    public FilesDto readDataroomById(int id) {
        return dataroomMapper.readDataroomById(id);
    }

    public List<FilesDto> getFilesByFolder(Integer departmentId, Integer teamId) {
        // 파라미터 디버깅
        System.out.println("Received departmentId: " + departmentId + ", teamId: " + teamId);

        List<FilesDto> files = Collections.emptyList();

        if (departmentId != null) {
            // DB 쿼리 디버깅
            System.out.println("Fetching files by departmentId: " + departmentId);
            files = dataroomMapper.readFilesByDepartmentId(departmentId);
        } else if (teamId != null) {
            // DB 쿼리 디버깅
            System.out.println("Fetching files by teamId: " + teamId);
            files = dataroomMapper.readFilesByTeamId(teamId);
        }

        // 결과 디버깅
        System.out.println("Fetched files: " + (files != null ? files.size() : 0));

        return files;
    }

    public List<FilesDto> getFilesByDepartment(Integer departmentId) {
        return dataroomMapper.readFilesByDepartmentId(departmentId);
    }

    public List<FilesDto> getFilesByTeam(Integer teamId) {
        return dataroomMapper.readFilesByTeamId(teamId);
    }

    public boolean isDepartmentHead(int userId, int folderId) {
        // 폴더가 속한 department의 userId 가져옴
        Integer departmentOwnerId = dataroomMapper.readDepartmentOwnerId(folderId);
        return departmentOwnerId != null && departmentOwnerId == userId;
    }

    public boolean isTeamMember(int userId, int folderId) {
        // 폴더가 속한 팀 이름 가져오기
        String folderTeamName = dataroomMapper.readTeamNameByFolderId(folderId);
        UsersDto user = usersMapper.readUserById(userId);
        return folderTeamName != null && folderTeamName.equals(user.getTeam());
    }


    public String readTeamNameByFolderId(int folderId) {
        String teamName = dataroomMapper.readTeamNameByFolderId(folderId);
        if (teamName == null) {
            throw new RuntimeException("팀 이름을 찾을 수 없습니다. 폴더 ID: " + folderId);  // 예외 처리
        }
        return teamName;
    }







}