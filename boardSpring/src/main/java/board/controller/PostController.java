package board.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import board.dto.PostRequest;
import board.dto.PostResponse;
import board.dto.PostResponseCnt;
import board.service.PostService;
import board.service.UserService;

import java.util.List;

@RestController @RequestMapping("/api/posts") @RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final UserService userService;

    @PostMapping
    public PostResponse create(@RequestParam Long userId,
                               @RequestBody @Valid PostRequest req) {
        return PostResponse.from(
                postService.create(req, userService.findById(userId))
        );
    }

    @GetMapping
    public List<PostResponseCnt> list() {
        return postService.findAllWithCommentCount();    }

    @GetMapping("/{id}")
    public PostResponse detail(@PathVariable Long id) {
        return PostResponse.from(postService.findById(id));
    }
    
    @PutMapping("/{id}")                               // ★ 게시글 수정
    public PostResponse update(@PathVariable Long id,
                               @RequestBody @Valid PostRequest req) {
        return PostResponse.from(postService.update(id, req));
    }

    @DeleteMapping("/{id}")                            // ★ 게시글 삭제
    public void delete(@PathVariable Long id) {
        postService.delete(id);
    }
}
