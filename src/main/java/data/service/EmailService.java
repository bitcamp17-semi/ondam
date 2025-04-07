package data.service;

import data.dto.EmailDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
public class EmailService {
    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    JavaMailSender javaMailSender;
    @Value("${server.url}")
    private String serverUrl;

    public void sendMail(EmailDto emailDto) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(emailDto.getTo()); // 메일 수신자
            mimeMessageHelper.setSubject(emailDto.getSubject()); // 메일 제목
            mimeMessageHelper.setText(emailDto.getMessage(), true); // 메일 본문 내용, HTML 여부
            javaMailSender.send(mimeMessage);
            log.info("mail sent successfully");
        } catch (MessagingException e) {
            log.info("mail sending failed");
            throw new RuntimeException(e);
        }
    }

    public void signUpMail(String name, String loginId, String email, String password) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("loginId", loginId);
        context.setVariable("loginPassword", password);
        context.setVariable("loginPage", serverUrl+"/login");
        String message = templateEngine.process("infoMailForm", context);
        EmailDto emailDto = EmailDto.builder()
                .to(email)
                .subject("온담 회원가입 완료 및 로그인 정보 안내")
                .message(message)
                .build();
        sendMail(emailDto); // 메일 발송
    }
}