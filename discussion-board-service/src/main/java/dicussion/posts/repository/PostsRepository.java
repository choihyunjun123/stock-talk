package dicussion.posts.repository;

import dicussion.posts.domain.Posts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostsRepository extends JpaRepository<Posts, Long> {

    List<Posts> findByTitleContaining(String title);

    List<Posts> findByUserId(String userId);

    List<Posts> findByContentContaining(String content);

    Page<Posts> findAllBy(Pageable pageable);
}
