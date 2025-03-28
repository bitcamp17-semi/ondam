package data.controller;

import data.dto.UsersDto;
import data.service.ObjectStorageService;
import data.service.UsersService;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UsersController {
    @Autowired
    UsersService usersService;
    @Autowired
    ObjectStorageService storageService;

    @PostMapping("/createUser")
    public ResponseEntity<Object> createUser(
            @ModelAttribute UsersDto usersDto,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
//        if (usersService.isAdmin((Integer) session.getAttribute("userId"))) {
            try {
                // 이미지 파일 처리
                if (profileImage != null && !profileImage.isEmpty() && !profileImage.getOriginalFilename().equals("")) {
                    String imageUrl = storageService.uploadFile(storageService.getBucketName(), "users", profileImage);
                    usersDto.setProfileImage(imageUrl);
                }
                // 사용자 생성 로직
                boolean isCreated = usersService.createUser(usersDto);
                if (isCreated) {
                    response.put("status", "ok");
                    response.put("result", usersDto);  // 생성된 사용자 정보 반환
                    return new ResponseEntity<>(response, HttpStatus.CREATED);
                } else {
                    response.put("status", "fail");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            } catch (Exception e) {
                response.put("status", "fail");
                response.put("error", e.getMessage());
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        } else {
//            response.put("status", "fail");
//            response.put("error", "your not admin");
//            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/updateUser")
    public ResponseEntity<Object> updateUser(
            @ModelAttribute UsersDto paramDto,
            HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        try {
//            paramDto paramDto = usersService.readUserById((Integer) session.getAttribute("userId"));
            UsersDto usersDto = usersService.readUserById(paramDto.getId());
            usersDto.setPassword(usersService.hashingPassword(paramDto.getPassword()));
            usersService.updateUser(usersDto);
            response.put("status", "ok");
            response.put("result", "updated user");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/deactivateUser")
    public ResponseEntity<Object> deactivateUser(@RequestParam int userId) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        try {
            usersService.deactivateUser(userId);
            response.put("status", "ok");
            response.put("result", "deactivate user");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}