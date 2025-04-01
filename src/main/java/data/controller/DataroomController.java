package data.controller;

import data.dto.DataRoomDto;
import data.dto.FilesDto;
import data.mapper.DataroomMapper;
import data.service.DataroomService;
import data.service.ObjectStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    public String getFilesList(@RequestParam(value = "roomId", required = false) Integer roomId,
                               @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                               @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                               @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                               Model model) {
        // 페이지 및 사이즈 값 검증
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;

        // 데이터 조회
        List<FilesDto> files = dataroomService.readFilesByRoomId(
                roomId != null ? roomId : 0,
                keyword,
                (page - 1) * size,
                size
        );
        List<DataRoomDto> categories = dataroomService.readCategories();

        // Model에 값 추가
        model.addAttribute("files", files);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedRoomId", roomId); // 선택된 roomId
        model.addAttribute("keyword", keyword);      // 검색 키워드
        model.addAttribute("currentPage", page);     // 현재 페이지
        model.addAttribute("pageSize", size);        // 페이지 크기
        model.addAttribute("roomId", roomId);        // roomId를 명시적으로 추가

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

    @GetMapping("/detail/{id}")
    public String getFileDetail(@PathVariable int id, Model model) {
        try {
            // 1. 파일 상세 조회
            FilesDto file = dataroomService.readById(id);
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


    @GetMapping("/upload")
    public String showUploadPage(Model model) {
        // DataRoomDto 타입으로 categories 선언
        List<DataRoomDto> categories = dataroomService.readCategories();

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

    // 업로드 실행
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
