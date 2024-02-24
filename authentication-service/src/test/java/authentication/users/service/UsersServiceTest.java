package authentication.users.service;

import authentication.users.domain.Users;
import authentication.users.repository.RefreshTokenRepository;
import authentication.users.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

    @Mock
    private UsersRepository usersRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider tokenProvider;
    @InjectMocks
    private UsersService usersService;

    @BeforeEach
    void setUp() {
        usersService = new UsersService(usersRepository, refreshTokenRepository, emailService, passwordEncoder, tokenProvider);
    }

    @Test
    void isEmailAlreadyUsed() {
        String email = "sajun28@naver.com";
        when(usersRepository.findByEmailAndStatus(email, true)).thenReturn(Optional.of(new Users()));
        assertTrue(usersService.isEmailAlreadyUsed(email));
    }

    @Test
    void isEmailAlreadyNotUsed() {
        String email = "sajun20@naver.com";
        when(usersRepository.findByEmailAndStatus(email, true)).thenReturn(Optional.empty());
        assertFalse(usersService.isEmailAlreadyUsed(email));
    }

    @Test
    void whenEmailIsAlreadyUsed_registerShouldReturnFalse() {
        Users users = new Users();
        users.setEmail("sajun28@naver.com");
        when(usersRepository.findByEmailAndStatus(users.getEmail(), true)).thenReturn(Optional.of(new Users()));
        assertFalse(usersService.register(users));
        verify(usersRepository, never()).save(any());
    }
}