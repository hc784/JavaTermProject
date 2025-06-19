package board.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import board.dto.CommentRequest;
import board.entity.Comment;
import board.entity.Post;
import board.entity.User;
import board.repository.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service @RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepo;

    @Transactional
    public Comment create(CommentRequest req, Post post, User author) {
        Comment comment = Comment.builder()
                .content(req.getContent())
                .post(post)
                .author(author)
                .createdAt(LocalDateTime.now())
                .build();
        return commentRepo.save(comment);
    }

    public List<Comment> findByPost(Post post) {
        return commentRepo.findByPostOrderByCreatedAt(post);
    }
    
    /* ─── 댓글 수정 ─── */
    @Transactional
    public Comment update(Long id, User actor, CommentRequest req) {
        Comment c = findEditable(id, actor);      // ← 권한 확인
        c.update(req.getContent());
        return c;
    }

    /* ─── 댓글 삭제 ─── */
    @Transactional
    public void delete(Long id, User actor) {
        Comment c = findEditable(id, actor);      // ← 권한 확인
        commentRepo.delete(c);
    }

    /* ─── 작성자 or 관리자 권한 체크 ─── */
    private Comment findEditable(Long id, User actor) {
        Comment c = commentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        boolean editable = actor.isAdmin() ||
                           actor.getId().equals(c.getAuthor().getId());

        if (!editable)
            throw new IllegalStateException("수정/삭제 권한이 없습니다.");

        return c;
    }
}