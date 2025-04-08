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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/dataroom")
public class DataroomController {
    @Autowired
    private DataroomService dataroomService;

    @Autowired
    private ObjectStorageService objectStorageService;
    @Autowired
    private DataroomMapper dataroomMapper;
    @Autowired
    private UsersMapper usersMapper;

    /**
     * 파일 목록 + 카테고리 및 검색 기능 처리 (페이징 적용)
     * @param roomId 선택된 카테고리 ID (null = 전체)
     * @param keyword 검색어 (기본값 "")
     * @param page 현재 페이지 (기본값 1)
     * @param size 페이지당 항목 수 (기본값 10)
     * @param model 데이터를 템플릿에 전달
     * @return 파일 목록 페이지
     */
    @GetMapping
    public String getFilesList(HttpSession session, @RequestParam(value = "roomId", required = false) Integer roomId,
                               @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                               @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                               @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                               Model model) {


        int userid = (int) session.getAttribute("userid");
        UsersDto user = usersMapper.readUserById(userid);
        if(user == null) {
            return "redirect:/login";
        }

        // 페이지 및 사이즈 값 검증
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;

        // 서비스 호출을 통해 파일 목록 조회 및 카테고리 정보 가져오기
        List<FilesDto> files = dataroomService.readDataroomFilesByIdAndKeyword(
                roomId != null ? roomId : 0,// roomId가 null인 경우 전체 조회 (roomId = 0)
                keyword,
                (page - 1) * size,// 페이지 번호 → 데이터의 시작 위치 계산
                size // 한 페이지에 가져올 최대 데이터 수

        );
        List<DataRoomDto> categories = dataroomService.readDataroomCategories();

        // Model에 값 추가
        model.addAttribute("files", files); //데이터Model에 추가하여 뷰로 전달
        model.addAttribute("categories", categories); //자료실 카테고리
        model.addAttribute("selectedRoomId", roomId); // 선택된 roomId
        model.addAttribute("keyword", keyword);      // 검색 키워드
        model.addAttribute("currentPage", page);     // 현재 페이지
        model.addAttribute("pageSize", size);        // 페이지 크기
        model.addAttribute("roomId", roomId);        // roomId를 명시적으로 추가
        model.addAttribute("department", user.getDepartment()); //user의 department 를 가져옴

        return "dataroom/files";
    }

    /**
     * 파일 삭제 기능
     * @param id 삭제할 파일의 ID
     * @return 파일 목록으로 리다이렉트
     */

    @PostMapping("/{id}")
    public String deleteFilePost(@PathVariable int id) {
        dataroomService.deleteFileAndCloud(id); // 삭제 로직 처리
        return "redirect:/api/dataroom"; // 삭제 후 목록 페이지로 리다이렉트
    }

    // 파일 상세 정보 조회 REST API (Read)
    @GetMapping("/detail/{id}")
    public String getFileDetail(@PathVariable int id, Model model) {
        try {
            // 1. 파일 상세 조회
            FilesDto file = dataroomService.readDataroomById(id);
            if (file == null) {
                throw new IllegalArgumentException("파일이 존재하지 않습니다. ID: " + id);
            }

            // 2. Presigned URL 생성 및 디버깅
            String fileURL = objectStorageService.generatePresignedURL(
                    objectStorageService.getBucketName(),
                    file.getPath(),
                    file.getName(),
                    60
            );
            System.out.println("Generated Presigned URL: " + fileURL); // 로그 출력으로 유효성 확인

            // 3. 모델에 데이터 추가
            model.addAttribute("file", file);
            model.addAttribute("fileURL", fileURL);

            return "dataroom/detail";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "파일 상세 정보를 불러오는 중 문제가 발생했습니다.");
            return "error/404";
        }
    }

    // 업로드 페이지 보여주기 (Create 준비)
    @GetMapping("/upload")
    public String showUploadPage(Model model) {
        // DataRoomDto 타입으로 categories 선언
        List<DataRoomDto> categories = dataroomService.readDataroomCategories();

        // 기본 값 처리
        if (categories == null || categories.isEmpty()) {
            // DataRoomDto 객체 생성
            categories = Arrays.asList(
                    new DataRoomDto(1, "Category1"),
                    new DataRoomDto(2, "Category2")
            );
        }

        // categories를 모델에 추가
        model.addAttribute("categories", categories);
        return "dataroom/upload";
    }


    // 파일 업로드 저장 처리 REST API (Create)
    @PostMapping("/upload")
    public ResponseEntity<Object> uploadFile(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("roomId") Integer roomId,
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new LinkedHashMap<>();
        try {
            // ncloud 업로드 및 DB 저장 실행
            dataroomService.uploadFileAndSaveToDB(
                    objectStorageService.getBucketName(),  // 버킷 이름
                    "dataroom",                           // 저장 경로
                    file,                                 // 업로드된 파일
                    title,                                // 제목
                    description,                          // 설명
                    roomId                                // 카테고리 ID
            );

            // 성공 시 응답
            response.put("status", "ok");
            response.put("message", "파일 업로드 및 저장 성공");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            // 실패 시 응답
            response.put("status", "fail");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
