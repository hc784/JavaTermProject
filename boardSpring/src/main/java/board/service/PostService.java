package board.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import board.dto.PostRequest;
import board.dto.PostResponse;
import board.dto.PostResponseCnt;
import board.entity.Post;
import board.entity.User;
import board.repository.CommentRepository;
import board.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service @RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepo;
    private final CommentRepository commentRepository;

    @Transactional
    public Post create(PostRequest req, User author) {
        Post post = Post.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .author(author)
                .createdAt(LocalDateTime.now())
                .build();
        return postRepo.save(post);
    }

    public List<Post> findAll() { return postRepo.findAll(); }

    public Post findById(Long id) {
        return postRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
    }
    
    public List<PostResponseCnt> findAllWithCommentCount() {
        return postRepo.findAll().stream()
                .map(p -> PostResponseCnt.from(
                        p,
                        commentRepository.countByPostId(p.getId())
                ))
                .toList();
    }
    
    @Transactional          // ★ 게시글 수정
    public Post update(Long postId, PostRequest req) {
        Post post = findById(postId);            // 존재 확인
        post.update(req.getTitle(), req.getContent());
        return post;                             // flush 시점에 업데이트
    }

    @Transactional          // ★ 게시글 삭제
    public void delete(Long postId) {
        Post post = findById(postId);            // 없는 ID 처리
        postRepo.delete(post);
    }

    
}
