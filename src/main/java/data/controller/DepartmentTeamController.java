package data.controller;

import data.dto.DepartmentDto;
import data.dto.TeamDto;
import data.service.DepartmentService;
import data.service.TeamService;
import data.service.UsersService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dep")
public class DepartmentTeamController {
    @Autowired
    DepartmentService departmentService;
    @Autowired
    TeamService teamService;
    @Autowired
    UsersService usersService;

    @GetMapping("/createDep")
    public ResponseEntity<Object> createDep(
            @RequestParam String name,
            @RequestParam int userId
    ) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        if (usersService.isAdmin(userId)) {
            try {
                departmentService.createDep(userId,name);
                response.put("status", "ok");
                response.put("result", "success");
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            } catch (Exception e) {
                response.put("status", "error");
                response.put("result", e.getMessage());
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            response.put("status", "fail");
            response.put("error", "you are not admin");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/readDeps")
    public ResponseEntity<Object> readDeps() {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            List<DepartmentDto> depList = departmentService.readAllDeps();
            response.put("status", "ok");
            response.put("result", depList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/createTeam")
    public ResponseEntity<Object> createTeam(
            @RequestParam int departmentId,
            @RequestParam String name
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            teamService.createTeam(departmentId,name);
            response.put("status", "ok");
            response.put("result", "success");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/readTeams")
    public ResponseEntity<Object> readTeams(@RequestParam int departmentId) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            List<TeamDto> teamList = teamService.readTeamsByDepId(departmentId);
            response.put("status", "ok");
            response.put("result", teamList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}