package authentication.users.service;

import authentication.users.domain.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(Users users) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(users.getEmail());
        message.setSubject("회원가입 인증 메일");
        message.setText("인증번호는 : " + users.getEmailCheckToken() + " 입니다.");
        mailSender.send(message);
    }
}
