package dicussion.posts.service;

import dicussion.posts.domain.Posts;
import dicussion.posts.repository.PostsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PostsService {

    private final PostsRepository postsRepository;

    public PostsService(PostsRepository postsRepository) {
        this.postsRepository = postsRepository;
    }

    public boolean write(Posts posts) {
        posts.setCreatedAt(LocalDate.now());
        posts.setStatus(true);
        postsRepository.save(posts);
        return true;
    }

    public List<Posts> findTitle(String title) {
        return postsRepository.findByTitleContaining(title);
    }

    public List<Posts> findWriter(String writer) {
        return postsRepository.findByUserId(writer);
    }

    public List<Posts> findContent(String content) {
        return postsRepository.findByContentContaining(content);
    }

    public Page<Posts> findAll(Pageable pageable) {
        return postsRepository.findAllBy(pageable);
    }
}
