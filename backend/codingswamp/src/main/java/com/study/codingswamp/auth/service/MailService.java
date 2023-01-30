package com.study.codingswamp.auth.service;

import com.study.codingswamp.auth.service.request.MailAuthenticationRequest;
import com.study.codingswamp.auth.service.response.MailAuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender emailSender;
    private static final String fromEmail = "seediu95@gmail.com";

    private String authCode;

    public MailAuthenticationResponse sendEmail(MailAuthenticationRequest request) {
        String email = request.getEmail();
        //메일전송에 필요한 정보 설정
        MimeMessage emailForm = createEmailForm(email);
        //실제 메일 전송
        emailSender.send(emailForm);
        return new MailAuthenticationResponse(email, authCode);
    }

    private MimeMessage createEmailForm(String toEmail) {
        createCode();
        String title = "코딩의늪 회원가입 인증 번호";

        MimeMessage message = emailSender.createMimeMessage();
        try {
            message.addRecipients(MimeMessage.RecipientType.TO, toEmail);
            message.setSubject(title);
            message.setFrom(fromEmail);
            message.setText(setContext(authCode), "utf-8", "html");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        return message;
    }

    private void createCode() {
        Random random = new Random();
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(3);

            switch (index) {
                case 0:
                    key.append((char) (random.nextInt(26) + 97));
                    break;
                case 1:
                    key.append((char) (random.nextInt(26) + 65));
                    break;
                case 2:
                    key.append(random.nextInt(9));
                    break;
            }
        }
        authCode = key.toString();
    }

    private String setContext(String authCode) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<body>" +
                "<div style=\"margin:100px;\">" +
                "     <h1> 안녕하세요.</h1>" +
                "     <h1> 모각코 모집 및 운영 서비스 코딩의늪 입니다.</h1>" +
                "     <br>" +
                "         <p> 아래 코드를 회원가입 창으로 돌아가 입력해주세요.</p>" +
                "     <br>" +
                "     <div align=\"center\" style=\"border:1px solid black; font-family:verdana;\">" +
                "     <h3 style=\"color:blue\"> 회원가입 인증 코드 입니다. </h3>" +
                "     <div style=\"font-size:130%\">" + authCode + "</div>" +
                "     </div>" +
                "     <br/>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
