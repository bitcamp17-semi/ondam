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
        List<DataRoomDto> folders = dataroomService.getSubFolders(parentId);
        for (DataRoomDto f : folders) {
            f.setHasChild(dataroomService.hasChild(f.getId()));
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
    @ResponseBody
    @GetMapping("/files")
    public List<FilesDto> getFilesByRoomId(@RequestParam int roomId) {
        return dataroomService.getFilesByRoomId(roomId);
    }



}
