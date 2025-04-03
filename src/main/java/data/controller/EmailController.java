package data.controller;

import data.dto.EmailDto;
import data.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mail")
public class EmailController {
    @Autowired
    EmailService emailService;

    @PostMapping("/sendMail")
    public ResponseEntity sendMail() {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        try {
            EmailDto emailDto = EmailDto.builder()
                    .to("jjw6963@gmail.com")
                    .subject("테스트 메일 제목")
                    .message("테스트 메일 본문")
                    .build();
            emailService.sendMail(emailDto);
            response.put("status", "ok");
            response.put("result", "send successful");
            return new ResponseEntity(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", e);
            return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
