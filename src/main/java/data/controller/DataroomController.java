package data.controller;

import data.dto.DataRoomDto;
import data.dto.FilesDto;
import data.dto.UsersDto;
import data.mapper.DataroomMapper;
import data.mapper.UsersMapper;
import data.service.DataroomService;
import data.service.ObjectStorageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Controller
@RequestMapping("/api/dataroom")
public class DataroomController {
    @Autowired
    private DataroomService dataroomService;
    @Autowired
    private JdbcTemplate jdbcTemplate; // Spring JdbcTemplate 사용
    @Autowired
    private ObjectStorageService objectStorageService;
    @Autowired
    private DataroomMapper dataroomMapper;
    @Autowired
    private UsersMapper usersMapper;

    //기본 페이지로 입장

    @GetMapping
    public String showStoragePage(Model model) {
        List<DataRoomDto> folders = dataroomService.getAllFolders();
        model.addAttribute("folders", folders);
        return "layout/storage/storage";
    }

    @ResponseBody
    @GetMapping("/folders/json")
    public List<DataRoomDto> getAllFoldersJson() {
        List<DataRoomDto> folders = dataroomService.getAllFolders();
        for (DataRoomDto f : folders) {
            f.setHasChild(dataroomService.hasChild(f.getId()));
        }
        return folders;
    }

    @ResponseBody
    @GetMapping("/subfolders/json/{parentId}")
    public List<DataRoomDto> getSubFoldersJson(@PathVariable int parentId) {
        System.out.println("parentId: " + parentId); // 요청된 parentId 확인
        List<DataRoomDto> folders = dataroomService.getSubFolders(parentId);
        System.out.println("folders: " + folders); // 반환된 폴더 리스트 확인

        for (DataRoomDto f : folders) {
            boolean hasChild = dataroomService.hasChild(f.getId());
            System.out.println("Folder ID: " + f.getId() + ", hasChild: " + hasChild); // 각 폴더의 hasChild 상태 확인
            f.setHasChild(hasChild);
        }

        return folders;
    }

    @ResponseBody
    @PostMapping("/addFolderAjax")
    public ResponseEntity<?> addFolderAjax(@RequestBody DataRoomDto folder) {
        dataroomService.addFolder(folder);
        return ResponseEntity.ok().build();
    }

    @ResponseBody
    @PostMapping("/deleteFolderAjax")
    public ResponseEntity<?> deleteFolderAjax(@RequestParam int folderId) {
        dataroomService.deleteFolder(folderId);
        return ResponseEntity.ok().build();
    }

    @ResponseBody
    @GetMapping("/checkFolderExists")
    public boolean checkFolderExists(@RequestParam String folderName, @RequestParam int parentId) {
        return dataroomService.checkFolderExists(folderName, parentId);
    }

    @ResponseBody
    @GetMapping("/hasChild")
    public boolean hasChild(@RequestParam int folderId) {
        return dataroomService.hasChild(folderId);
    }

    // ✅ 파일 목록 조회: 특정 roomId 에 속한 파일들 반환
    @GetMapping("/files")
    public ResponseEntity<List<FilesDto>> getFiles(@RequestParam(value = "teamId", required = false) Integer teamId) {
        List<FilesDto> files = dataroomService.getFilesByTeam(teamId);
        return ResponseEntity.ok(files);
    }
    /*@GetMapping("/files")
    public ResponseEntity<List<Map<String, Object>>> getFilesWithAuthorName(@RequestParam(value = "teamId", required = false) Integer teamId) {
        // 팀 ID로 파일 리스트 가져오기
        List<FilesDto> files = dataroomService.getFilesByTeam(teamId);

        List<Map<String, Object>> result = new ArrayList<>();

        // 파일 리스트를 Map으로 변환하면서 authorName 추가
        for (FilesDto file : files) {
            Map<String, Object> fileMap = new HashMap<>();
            fileMap.put("id", file.getId());
            fileMap.put("name", file.getName());
            fileMap.put("path", file.getPath());
            fileMap.put("title", file.getTitle());
            fileMap.put("category", file.getCategory());
            fileMap.put("createdAt", file.getCreatedAt());
            fileMap.put("updatedAt", file.getUpdatedAt());
            fileMap.put("authorId", file.getAuthorId());
            fileMap.put("type", file.getType());
            fileMap.put("departmentId", file.getDepartmentId());
            fileMap.put("teamId", file.getTeamId());

            // authorId로 사용자 이름 조회
            String authorName = dataroomMapper.readUserNameById(file.getAuthorId());
            fileMap.put("authorName", authorName);  // authorName 추가

            result.add(fileMap);
        }

        return ResponseEntity.ok(result);
    }*/





    @ResponseBody
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("upload") MultipartFile upload,
                                        @RequestParam(value = "teamId", required = false) Integer teamId,
                                        @RequestParam("title") String title,
                                        @RequestParam("category") String category,
                                        @RequestParam(value = "departmentId", required = false) Integer departmentId,
                                        HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 필요");
            }
            System.out.println("업로드 시작: " + upload.getOriginalFilename());

            String directoryPath = "dataroom";
            dataroomService.uploadFileAndSaveToDB(
                    objectStorageService.getBucketName(),
                    directoryPath,
                    upload,
                    title,
                    category,
                    departmentId,
                    teamId,
                    userId
            );

            // JSON 형식으로 응답 반환
            System.out.println("업로드 성공 응답: 업로드 성공");
            return ResponseEntity.ok().body(new HashMap<String, String>() {{
                put("message", "업로드 성공");
            }});
        } catch (Exception e) {
            System.err.println("업로드 실패: " + e.getMessage());  // 예외 로그
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("업로드 실패: " + e.getMessage());
        }
    }

    @PostMapping("/deleteFiles")
    @ResponseBody
    public ResponseEntity<?> deleteFiles(@RequestParam(value = "id") int id) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            FilesDto dto = dataroomService.readDataroomById(id);
            objectStorageService.deleteFile(objectStorageService.getBucketName(),"dataroom", dto.getPath());
            dataroomService.deleteFiles(id);
            response.put("status", "ok");
            response.put("result", "delete success");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




}
