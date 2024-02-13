package authentication.users.service;

import authentication.users.domain.Users;
import authentication.users.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UsersService(UsersRepository usersRepository, EmailService emailService, BCryptPasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입
    @Transactional
    public boolean register(Users users) {
        if (isEmailAlreadyUsed(users.getEmail())) return false;
        prepareUserForRegistration(users);
        usersRepository.save(users);
        return true;
    }

    // 이메일 중복 확인
    public boolean isEmailAlreadyUsed(String email) {
        return usersRepository.findByEmailAndStatus(email, true).isPresent();
    }

    // 회원가입 준비과정
    private void prepareUserForRegistration(Users users) {
        mailSend(users);
        encodePassword(users);
        setUserStatusAndTimestamp(users);
    }

    // 인증 이메일 전송
    private void mailSend(Users users) {
        users.setEmailCheckToken(UUID.randomUUID().toString());
        emailService.sendEmail(users);
    }

    // 비밀번호 암호화
    private void encodePassword(Users users) {
        String encodedPassword = passwordEncoder.encode(users.getPassword());
        users.setPassword(encodedPassword);
    }

    // 유저 상태 변화
    private void setUserStatusAndTimestamp(Users users) {
        users.setStatus(true);
        users.setCreatedAt(LocalDateTime.now());
    }

    // 이메일 인증
    public boolean verifyEmail(String email, String token) {
        return usersRepository.findByEmail(email)
                .filter(user -> user.getEmailCheckToken().equals(token))
                .map(user -> {
                    user.setEmailVerified(true);
                    usersRepository.save(user);
                    return true;
                })
                .orElse(false);
    }
}
