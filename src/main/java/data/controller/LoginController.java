package data.controller;

import data.dto.UsersDto;
import data.service.UsersService;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {
    @Autowired
    UsersService usersService;

    @PostMapping("/login")
    public ResponseEntity<Object> login(
            @RequestParam(value = "loginId") String loginId,
            @RequestParam(value = "password") String password,
            HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            if (loginId == null || loginId.trim().isEmpty()) {
                response.put("status", "fail");
                response.put("result", "loginId is required");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            UsersDto checkingUser = usersService.readUserByLoginId(loginId);
            if (checkingUser == null) {
                response.put("status", "fail");
                response.put("result", "invalid loginId");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            if (BCrypt.checkpw(password, checkingUser.getPassword())) {
                response.put("status", "ok");
                response.put("result", "login successful");
                session.setAttribute("userId", checkingUser.getId());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("status", "fail");
                response.put("result", "incorrect password");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", "An unexpected error occurred: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping("/logout")
    public ResponseEntity<Object> logout(HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            session.removeAttribute("userId");
            session.invalidate();
            response.put("status", "ok");
            response.put("result", "logout successful");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", "An unexpected error occurred: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/checkLogin")
    public ResponseEntity<Map<String, Object>> checkLogin(HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (session.getAttribute("userId") != null) {
            response.put("status", "ok");
            response.put("result", "logged in");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("status", "error");
            response.put("result", "not logged in");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }
}