package data.controller;

import data.dto.UsersDto;
import data.service.EmailService;
import data.service.ObjectStorageService;
import data.service.UsersService;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

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
            @RequestParam(value = "upload", required = false) MultipartFile upload,
            HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        if (usersService.isAdmin((Integer) session.getAttribute("userId"))) {
            try {
                // 이미지 파일 처리
                if (upload != null && !upload.isEmpty() && !upload.getOriginalFilename().equals("")) {
                    String imageUrl = storageService.uploadFile(storageService.getBucketName(), "users", upload);
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
            }
        } else {
            response.put("status", "fail");
            response.put("error", "you're not admin");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
    }

    // 본인 수정 (비밀번호 수정. 추후 더 필요하면 usersDto에 값 추가)
    @PostMapping("/updateUser")
    public ResponseEntity<Object> updateSelf(
            @ModelAttribute UsersDto paramDto,
            HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        try {
            UsersDto usersDto = usersService.readUserById((Integer) session.getAttribute("userId"));
//            UsersDto usersDto = usersService.readUserById(paramDto.getId());
            usersDto.setPassword(usersService.hashingPassword(paramDto.getPassword()));
            usersDto.setEmail(paramDto.getEmail());
            usersDto.setPhone(paramDto.getPhone());
            usersDto.setAddr(paramDto.getAddr());
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

    // 관리자 수정
    @PostMapping("/updateUserAdmin")
    public ResponseEntity<Object> updateUser(
            @ModelAttribute UsersDto paramDto,
            @RequestParam(value = "upload", required = false) MultipartFile upload) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        try {
            UsersDto usersDto = usersService.readUserById(paramDto.getId());
            // 이미지 파일 처리 (기존 파일과 같으면 실행하지 않는다)
            if (upload != null && !upload.isEmpty() && !upload.getOriginalFilename().equals("") && !upload.getOriginalFilename().equals(usersDto.getProfileImage())) {
                // 기존 파일 제거
                storageService.deleteFile(storageService.getBucketName(), "users", usersDto.getProfileImage());
                // 새 파일 업로드
                String imageUrl = storageService.uploadFile(storageService.getBucketName(), "users", upload);
                usersDto.setProfileImage(imageUrl);
            }
            usersDto.setName(paramDto.getName());
            usersDto.setEmail(paramDto.getEmail());
            usersDto.setDepartmentId(paramDto.getDepartmentId());
            usersDto.setTeam(paramDto.getTeam());
            usersDto.setPosition(paramDto.getPosition());
            usersDto.setPassword(paramDto.getPassword());
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

    @GetMapping("/readUserBySession")
    public ResponseEntity<Object> getUserBySession(HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("status", "ok");
            UsersDto usersDto = usersService.readUserById((Integer) session.getAttribute("userId"));
            usersDto.setPassword(null);
            response.put("result", usersDto);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/readUserById")
    public ResponseEntity<Object> getUserById(int userId) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("status", "ok");
            UsersDto usersDto = usersService.readUserById(userId);
            usersDto.setPassword(null);
            response.put("result", usersDto);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/deactivateUser")
    public ResponseEntity<Object> deactivateUser(@RequestParam(value = "userId") int userId, HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        if (usersService.isAdmin((Integer) session.getAttribute("userId"))) {
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
        } else {
            response.put("status", "fail");
            response.put("error", "you're not admin");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/readUsersByDep")
    public ResponseEntity<Object> readUsersByDep(@RequestParam(value = "departmentId") int departmentId,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Map<String, Object> result = new HashMap<>();
            int offset = (page - 1) * size;
            List<UsersDto> list = usersService.readUsersByDep(departmentId, offset, size);
            int totalCnt = usersService.readCountUsersByDep(departmentId);
            result.put("totalCnt", totalCnt);
            result.put("list", list);
            response.put("status", "ok");
            response.put("result", result);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/readAllUsersByDep")
    public ResponseEntity<Object> readAllUsersByDep(@RequestParam("departmentId") int departmentId) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            List<UsersDto> userList = usersService.readAllUsersByDep(departmentId);
            List<UsersDto> list = new ArrayList<>();
            for (UsersDto user:userList) {
                user.setPassword(null);
                list.add(user);
            }
            response.put("status", "ok");
            response.put("result", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/readUsersByTeam")
    public ResponseEntity<Object> readUsersByTeam(@RequestParam(value = "team") String team) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Map<String, Object> result = new HashMap<>();
            List<UsersDto> list = usersService.readUsersByTeam(team);
            response.put("status", "ok");
            response.put("result", list);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/readUsersByTeamId")
    public ResponseEntity<Object> readUsersByTeamId(@RequestParam(value = "teamId") int teamId) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Map<String, Object> result = new HashMap<>();
            List<UsersDto> list = usersService.readUsersByTeamId(teamId);
            response.put("status", "ok");
            response.put("result", list);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/deleteUser")
    public ResponseEntity<Object> deleteUser(@RequestParam(value = "userId") int userId) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            usersService.deleteUser(userId);
            response.put("status", "ok");
            response.put("result", "delete user");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/deactivateUsers")
    public ResponseEntity<Object> deactivateUsers(@RequestParam(value = "userList") List<Integer> userList) {
        Map<String, Object> response = new HashMap<>();
        try {
            for (Integer userId : userList) {
                usersService.deactivateUser(userId);
            }
            response.put("status", "ok");
            response.put("result", "deactivate users");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/deleteUsers")
    public ResponseEntity<Object> deleteUsers(@RequestParam(value = "userList") List<Integer> userList) {
        Map<String, Object> response = new HashMap<>();
        try {
            for (Integer userId : userList) {
                UsersDto dto = usersService.readUserById(userId);
                String img = dto.getProfileImage();
                if (!img.isEmpty()) {
                    storageService.deleteFile(storageService.getBucketName(), "users", img);
                }
            }
            usersService.deleteUsers(userList);
            response.put("status", "ok");
            response.put("result", "deactivate users");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/readUsersByName")
    public ResponseEntity<Object> readUsersByName(@RequestParam(value = "name") String name) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            List<UsersDto> list = usersService.readUsersByName(name);
            response.put("status", "ok");
            response.put("result", list);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/readAllUsers")
    public ResponseEntity<Object> readAllUsers() {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            List<UsersDto> userList = usersService.readAllActiveUsers();
            List<UsersDto> list = new ArrayList<>();
            for (UsersDto user:userList) {
                user.setPassword(null);
                list.add(user);
            }
            response.put("status", "ok");
            response.put("result", list);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/checkPassword")
    public ResponseEntity<Object> checkPassword(@RequestParam(value = "password") String password, HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            int userId = (Integer) session.getAttribute("userId");
            if (BCrypt.checkpw(password, usersService.readUserById(userId).getPassword())) {
                response.put("status", "ok");
                response.put("result", "password is matched");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("status", "fail");
                response.put("result", "password does not match");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}