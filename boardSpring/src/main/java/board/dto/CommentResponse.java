package board.dto;

import board.entity.Comment;
import lombok.Builder;

@Builder
public record CommentResponse(Long id,
                              String content,
                              Long   authorId,
                              String author,
                              String createdAt) {

    public static CommentResponse from(Comment c) {
        return CommentResponse.builder()
                .id(c.getId())
                .content(c.getContent())
                .authorId(c.getAuthor().getId())
                .author(c.getAuthor().getUsername())
                .createdAt(c.getCreatedAt().toString())
                .build();
    }
}

