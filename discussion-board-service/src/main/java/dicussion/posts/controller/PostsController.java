package dicussion.posts.controller;

import dicussion.posts.domain.Posts;
import dicussion.posts.service.PostsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostsController {

    private final PostsService postsService;

    public PostsController(PostsService postsService) {
        this.postsService = postsService;
    }

    private ResponseEntity<?> buildResponse(boolean success, String message) {
        String responseMessage = "{\"message\": \"" + message + "\"}";
        return success ? ResponseEntity.ok(responseMessage) : ResponseEntity.badRequest().body(responseMessage);
    }

    @PostMapping("/write")
    public ResponseEntity<?> write(@RequestBody Posts posts) {
        boolean success = postsService.write(posts);
        return buildResponse(success, success ? "성공" : "실패");
    }

    @GetMapping("/find-title")
    public ResponseEntity<?> findByTitle(@RequestParam("title") String title) {
        List<Posts> foundPost = postsService.findTitle(title);
        if (foundPost.isEmpty()) {
            return buildResponse(false, "없음");
        } else {
            return ResponseEntity.ok(foundPost);
        }
    }

    @GetMapping("/find-writer")
    public ResponseEntity<?> findByWriter(@RequestParam("writer") String writer) {
        List<Posts> foundPost = postsService.findWriter(writer);
        if (foundPost.isEmpty()) {
            return buildResponse(false, "없음");
        } else {
            return ResponseEntity.ok(foundPost);
        }
    }

    @GetMapping("/find-content")
    public ResponseEntity<?> findByContent(@RequestParam("content") String content) {
        List<Posts> foundPost = postsService.findContent(content);
        if (foundPost.isEmpty()) {
            return buildResponse(false, "없음");
        } else {
            return ResponseEntity.ok(foundPost);
        }
    }

    @GetMapping("/find-all")
    public ResponseEntity<?> findAll(Pageable pageable) {
        Page<Posts> foundPost = postsService.findAll(pageable);
        if (foundPost.isEmpty()) {
            return buildResponse(false, "없음");
        } else {
            return ResponseEntity.ok(foundPost);
        }
    }
}
