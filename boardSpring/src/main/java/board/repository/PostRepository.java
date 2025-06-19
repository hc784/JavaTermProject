package board.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import board.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> { }
