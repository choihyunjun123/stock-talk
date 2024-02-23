package authentication.users.repository;

import authentication.users.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmailAndStatus(String email, boolean status);

    Optional<Users> findByEmail(String email);

    Optional<Users> findById(Long id);
}
