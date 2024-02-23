package authentication.users.service;

import authentication.users.domain.Users;
import authentication.users.dto.LoginRequest;
import authentication.users.dto.LoginResponse;
import authentication.users.repository.RefreshTokenRepository;
import authentication.users.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(UsersService.class);

    private static final String BEARER_PREFIX = "Bearer ";

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
        logger.info("Fetching user by ID: {}", email);
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
        return usersRepository.findByEmail(loginRequest.getEmail())
                .filter(user -> passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
                .map(this::generateLoginResponse)
                .orElseThrow(() -> new AuthenticationException("회원정보가 없습니다."));
    }

    // 토큰 생성
    private LoginResponse generateLoginResponse(Users users) {
        String accessToken = tokenProvider.generateAccessToken(users);
        String refreshToken = tokenProvider.generateRefreshToken(users);
        return new LoginResponse(accessToken, refreshToken);
    }

    public String accessValidate(String token) {
        return validateToken(token, false);
    }

    public String refreshValidate(String token) {
        return validateToken(token, true);
    }

    private String validateToken(String token, boolean isRefreshToken) {
        String result = getTokenFromBearerString(token)
                .map(t -> {
                    try {
                        tokenProvider.validateToken(t);
                        if (isRefreshToken) {
                            return refreshTokenRepository.findByToken(t)
                                    .map(refreshToken -> generateLoginResponse(refreshToken.getUsers()).getAccessToken())
                                    .orElse("리프레시 토큰이 유효하지 않습니다.");
                        }
                        return "토큰이 유효합니다.";
                    } catch (Exception e) {
                        return e.getMessage();
                    }
                }).orElse("토큰이 없습니다.");
        return result;
    }

    private Optional<String> getTokenFromBearerString(String token) {
        if (token != null && token.startsWith(BEARER_PREFIX)) {
            return Optional.of(token.substring(BEARER_PREFIX.length()));
        }
        return Optional.empty();
    }
}
