package authentication.users.domain;

import authentication.users.domain.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Table(name = "refresh_token")
@Getter
@Setter
@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Users users;

    @Column(name = "token")
    private String token;

    @Column(name = "is_valid")
    private boolean isValid;

    @Column(name = "expiration_date")
    private Date expirationDate;

    public RefreshToken(Users users, String token, Date expirationDate) {
        this.users = users;
        this.token = token;
        this.expirationDate = expirationDate;
        this.isValid = true;
    }

    public RefreshToken() {

    }
}
