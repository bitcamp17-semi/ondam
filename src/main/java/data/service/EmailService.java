package data.service;

import data.dto.EmailDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

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
}