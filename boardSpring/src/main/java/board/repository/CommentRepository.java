package board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import board.entity.Comment;
import board.entity.Post;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAt(Post post);
    long countByPostId(Long postId);
}
