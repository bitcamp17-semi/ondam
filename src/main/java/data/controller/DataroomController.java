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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
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
    public ResponseEntity<List<FilesDto>> getFiles(
            @RequestParam(value = "departmentId", required = false) Integer departmentId,
            @RequestParam(value = "teamId", required = false) Integer teamId
    ) {
        List<FilesDto> files;

        if (teamId != null) {
            files = dataroomService.getFilesByTeam(teamId);
        } else if (departmentId != null) {
            files = dataroomService.getFilesByDepartment(departmentId);
        } else {
            files = Collections.emptyList(); // 또는 예외 처리
        }

        return ResponseEntity.ok(files);
    }


    /*@ResponseBody
    @GetMapping("/files")
    public ResponseEntity<List<Map<String, Object>>> getFiles(
            @RequestParam(value = "departmentId", required = false) Integer departmentId,
            @RequestParam(value = "teamId", required = false) Integer teamId) {

        List<Map<String, Object>> files = dataroomService.getFilesWithAuthorName(departmentId, teamId);
        return ResponseEntity.ok(files);
    }*/






    @ResponseBody
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("upload") MultipartFile upload,
                                        @RequestParam(value = "teamId", required = false) Integer teamId,
                                        @RequestParam("title") String title,
                                        @RequestParam("category") String category,
                                        @RequestParam(value = "departmentId", required = false) Integer departmentId,
                                        HttpSession session) {
        System.out.println("업로드 요청 받음");
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

            System.out.println("업로드 성공");
            return ResponseEntity.ok().body(new HashMap<String, String>() {{
                put("message", "업로드 성공");
            }});
        } catch (Exception e) {
            System.err.println("업로드 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("업로드 실패: " + e.getMessage());
        }
    }


    @GetMapping("/download")
    public ResponseEntity<Void> downloadFile(
            @RequestParam String path,    // 파일 경로
            @RequestParam String name     // 파일 원본 이름
    ) {
        String folder = "dataroom"; // 폴더명 고정 (필요한 경우 다르게 수정 가능)

        // presigned URL 생성
        String presignedUrl = objectStorageService.generatePresignedURL(
                objectStorageService.getBucketName(),
                folder + "/" + path,  // path는 저장된 파일의 경로
                name,                 // 파일 이름
                3600                  // 만료 시간 1시간
        );

        // presigned URL 확인
        System.out.println("presignedUrl: " + presignedUrl);

        // 헤더 설정: 파일 다운로드를 위한 Content-Disposition 추가
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=\"" + name + "\"");

        // 리다이렉트 응답 대신, 파일을 다운로드할 수 있도록 리턴
        return ResponseEntity.status(HttpStatus.FOUND) // 302 Found
                .location(URI.create(presignedUrl))      // presigned URL로 리다이렉트
                .headers(headers)                         // 헤더 설정 추가
                .build();
    }

    @PostMapping("/downloadMultiple")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> downloadMultipleFiles(@RequestParam("ids") List<Integer> ids) {
        Map<String, Object> response = new HashMap<>();
        try {
            String zipUrl = dataroomService.createZipAndGetUrl(ids); // zip 생성 후 URL 반환
            response.put("status", "ok");
            response.put("downloadUrl", zipUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/deleteMultiple")
    @ResponseBody
    public ResponseEntity<?> deleteMultipleFiles(@RequestParam("ids") List<Integer> ids) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            for (int id : ids) {
                FilesDto dto = dataroomService.readDataroomById(id);
                objectStorageService.deleteFile(objectStorageService.getBucketName(), "dataroom", dto.getPath());
                dataroomService.deleteFiles(id);
            }
            response.put("status", "ok");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }







    @PostMapping("/deleteFiles")
    @ResponseBody
    public ResponseEntity<?> deleteFiles(@RequestParam("id") int id) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            FilesDto dto = dataroomService.readDataroomById(id);

            // 스토리지에서 파일 삭제
            objectStorageService.deleteFile(
                    objectStorageService.getBucketName(),
                    "dataroom",
                    dto.getPath()
            );

            // DB에서 파일 정보 삭제
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


    @GetMapping("/accessFolder")
    public String checkAccessFolder(Model model, @RequestParam int folderId, HttpSession session) {
        // 로그인한 사용자 ID 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login"; // 로그인 안 되어 있으면 로그인 페이지로 리다이렉트
        }

        // 폴더에 대한 팀 이름 및 부서장 확인
        String folderTeamName = dataroomService.readTeamNameByFolderId(folderId);
        boolean isDepartmentHead = dataroomService.isDepartmentHead(userId, folderId);
        boolean isTeamMember = dataroomService.isTeamMember(userId, folderId);

        // 접근 권한 확인
        if (isDepartmentHead || isTeamMember) {
            model.addAttribute("hasAccess", true);
            return "dataroom/folderView"; // 폴더 보기 페이지로 이동
        } else {
            model.addAttribute("hasAccess", false);
            return "error/noAccess"; // 접근 권한 없음 페이지로 이동
        }
    }





}
