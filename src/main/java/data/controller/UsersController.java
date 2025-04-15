package data.controller;

import data.dto.ScheduleGroupDto;
import data.dto.UsersDto;
import data.service.EmailService;
import data.service.ObjectStorageService;
import data.service.ScheduleGroupMembersService;
import data.service.ScheduleGroupService;
import data.service.SchedulesService;
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
    @Autowired
    SchedulesService scheduleService;
    @Autowired
    ScheduleGroupService scheduleGroupService;
    @Autowired
    ScheduleGroupMembersService scheduleGroupMemberService;

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
                	Integer newUserId = usersDto.getId(); // 생성된 사용자 ID
                	
                	// '개인일정' 그룹 없으면 자동 생성
                    ScheduleGroupDto privateGroup = scheduleGroupService.readPrivateGroup(newUserId);
                    if (privateGroup == null) {
                        Map<String, Object> groupMap = new HashMap<>();
                        groupMap.put("name", "개인일정");
                        groupMap.put("color", "#28a745");
                        groupMap.put("ownerId", newUserId);
                        scheduleGroupService.scheGroupInsert(groupMap);
                    }

                    //'회사일정' 그룹 멤버가 아니면 자동 등록
                    Integer companyMemExist = scheduleGroupService.readCompanyGroupMember(newUserId);
                    if (companyMemExist == null) {
                        //'회사그룹'의 그룹 id 저장
                    	Integer companyGroupId = scheduleGroupService.readCompanyGroupId();
                        if (companyGroupId != null) {
                            Map<String, Object> companyMap = new HashMap<>();
                            companyMap.put("userId", newUserId);
                            companyMap.put("groupId", companyGroupId);
                            companyMap.put("color", "#ffa500");
                            
                            // Map 하나만 등록하더라도 리스트로 감싸서 넘기기 > scheGroupMemberInsert list를 반환하도록 되어있음
                            //List<Map<String, Object>> memberList = new ArrayList<>();
                            //memberList.add(memberMap);
                            //멤버 등록	
                            //scheduleGroupMemberService.scheGroupMemberInsert(memberList);
                            scheduleGroupMemberService.scheGroupMemberInsert(List.of(companyMap));
                            System.out.println("회사일정 그룹에 멤버 자동 추가 완료");
                        } else {
                            System.out.println("'회사일정' 그룹이 존재하지 않습니다.");
                        }
                    }
                	
                    //회원가입 시 선택한 부서가 일정 그룹 멤버로 추가
                    // '부서일정' 그룹 자동 등록 (부서ID 기반)
                    Integer departmentId = usersDto.getDepartmentId(); //생성한 유저의 departmentId 받기
                    if (departmentId != null) {
                        // 부서 일정 그룹 ID 조회
                        Integer buseoGroupId = scheduleGroupService.readBuseoGroupId(departmentId);
                        
                        // 그룹 ID가 존재하고, 아직 멤버가 아닌 경우만 등록
                        if (buseoGroupId != null) {
                        	Integer buseoMemberExist = scheduleGroupMemberService.readGroupMemExist(buseoGroupId, newUserId);
                            if (buseoMemberExist == null) {
                                Map<String, Object> buseoMap = new HashMap<>();
                                buseoMap.put("userId", newUserId);
                                buseoMap.put("groupId", buseoGroupId);
                                buseoMap.put("color", "#808080"); // 부서 그룹 기본 색 (원하면 변경 가능)

                                //List<Map<String, Object>> buseoList = new ArrayList<>();
                                //buseoList.add(buseoMap);
                                scheduleGroupMemberService.scheGroupMemberInsert(List.of(buseoMap));
                                System.out.println("부서일정 그룹에 멤버 자동 추가 완료");
                            }
                        } else {
                            System.out.println("해당 부서의 '부서일정' 그룹이 존재하지 않습니다.");
                        }
                    }
                    
                	
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
                                                 @RequestParam(value = "page", defaultValue = "1") int page,
                                                 @RequestParam(value = "size", defaultValue = "10") int size) {
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
            UsersDto dto = usersService.readUserById(userId);
            if (dto.getProfileImage() != null) {
                storageService.deleteFile(storageService.getBucketName(),"users",dto.getProfileImage());
            }
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
                if (img != null) {
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

    @GetMapping("/checkAdmin")
    public ResponseEntity<Object> checkAdmin(HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            int userId = (Integer) session.getAttribute("userId");
            if (usersService.isAdmin(userId)) {
                response.put("status", "ok");
                response.put("result", "isAdmin");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("status", "fail");
                response.put("result", "is not admin");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getFormers")
    public ResponseEntity<Object> getFormers(
            @RequestParam(value = "keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        int userId = (Integer) session.getAttribute("userId");
        try {
            if (usersService.isAdmin(userId)) {
                Map<String, Object> result = new HashMap<>();
                int offset = (page - 1) * size;
                List<UsersDto> list = usersService.readAllDeactivateUsersByKeyword(keyword, offset, size);
                int totalCnt = usersService.readCountDeactivateUsersByKeyword(keyword);
                result.put("list", list);
                result.put("totalCnt", totalCnt);
                response.put("status", "ok");
                response.put("result", result);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("status", "fail");
                response.put("result", "is not admin");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}