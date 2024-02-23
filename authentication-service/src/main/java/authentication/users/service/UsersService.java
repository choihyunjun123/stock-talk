package authentication.users.service;

import authentication.users.domain.RefreshToken;
import authentication.users.domain.Users;
import authentication.users.dto.LoginRequest;
import authentication.users.dto.LoginResponse;
import authentication.users.repository.RefreshTokenRepository;
import authentication.users.repository.UsersRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.sasl.AuthenticationException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public UsersService(UsersRepository usersRepository, RefreshTokenRepository refreshTokenRepository, EmailService emailService, BCryptPasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.usersRepository = usersRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
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
    @Async
    protected void prepareUserForRegistration(Users users) {
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

    // 로그인
    public LoginResponse login(LoginRequest loginRequest) throws AuthenticationException {
        Optional<Users> users = usersRepository.findByEmail(loginRequest.getEmail());
        Users users1 = users.orElse(null);
        assert users1 != null;
        if (!passwordEncoder.matches(loginRequest.getPassword(), users1.getPassword())) {
            throw new AuthenticationException("회원정보가 없습니다.");
        }
        ;
        return generateLoginResponse(users1);
    }

    // 토큰 생성
    private LoginResponse generateLoginResponse(Users users) {
        String accessToken = tokenProvider.generateAccessToken(users);
        String refreshToken = tokenProvider.generateRefreshToken(users);
        return new LoginResponse(accessToken, refreshToken);
    }

    public String accessValidate(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                tokenProvider.validateToken(token);
                return "토큰이 유효합니다.";
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        return "토큰이 없습니다.";
    }

    public String refreshValidate(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                tokenProvider.validateToken(token);
                Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
                RefreshToken refreshToken1 = refreshToken.orElse(null);
                assert refreshToken1 != null;
                Users users = refreshToken1.getUsers();
                generateLoginResponse(users);
                return generateLoginResponse(users).getAccessToken();
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        return "토큰이 없습니다.";
    }
}
