package authentication.users.controller;

import authentication.users.domain.Users;
import authentication.users.dto.EmailCheckRequest;
import authentication.users.dto.LoginRequest;
import authentication.users.dto.LoginResponse;
import authentication.users.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.sasl.AuthenticationException;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final UsersService usersService;

    @Autowired
    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    private ResponseEntity<String> buildResponse(boolean success, String message) {
        String responseMessage = "{\"message\": \"" + message + "\"}";
        return success ? ResponseEntity.ok(responseMessage) : ResponseEntity.badRequest().body(responseMessage);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Users users) {
        boolean success = usersService.register(users);
        return buildResponse(success, success ? "성공" : "이메일이 이미 사용 중입니다.");
    }

    @PostMapping("/check/email")
    public ResponseEntity<String> checkEmailToken(@RequestBody EmailCheckRequest emailCheckRequest) {
        boolean success = usersService.verifyEmail(emailCheckRequest.getEmail(), emailCheckRequest.getToken());
        return buildResponse(success, success ? "성공" : "실패");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) throws AuthenticationException {
        return ResponseEntity.ok(usersService.login(loginRequest));
    }

    @GetMapping("/access-token")
    public ResponseEntity<?> validateAccessToken(@RequestHeader(value = "Authorization") String accessToken) {
        return ResponseEntity.ok(usersService.accessValidate(accessToken));
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<?> validateRefreshToken(@RequestHeader(value = "Authorization") String refreshToken) {
        return ResponseEntity.ok("token :" + usersService.refreshValidate(refreshToken));
    }
}
