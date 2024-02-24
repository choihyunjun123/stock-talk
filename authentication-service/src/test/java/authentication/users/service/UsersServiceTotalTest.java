package authentication.users.service;

import authentication.users.domain.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UsersServiceTotalTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void registerUsedEmail() {
        Users users = new Users();
        users.setEmail("sajun28@naver.com");
        ResponseEntity<String> response = restTemplate.postForEntity("/users/register", users, String.class);
        assertTrue(Objects.requireNonNull(response.getBody()).contains("이메일이 이미 사용 중입니다."));
    }
}
