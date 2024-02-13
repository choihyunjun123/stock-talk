package authentication.users.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailCheckRequest {
    private String token;
    private String email;

    public EmailCheckRequest(String email, String token) {
        this.email = email;
        this.token = token;
    }

}
