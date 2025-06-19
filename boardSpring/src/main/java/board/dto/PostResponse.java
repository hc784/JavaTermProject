package board.dto;

import board.entity.Post;
import lombok.Builder;

@Builder
public record PostResponse(
        Long   id,
        String title,
        String content,
        Long   authorId,   // ★ 추가
        String author,     //   (username)
        String createdAt
) {
    public static PostResponse from(Post p) {
        return PostResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .content(p.getContent())
                .authorId(p.getAuthor().getId())       // ★ 추가
                .author(p.getAuthor().getUsername())
                .createdAt(p.getCreatedAt().toString())
                .build();
    }
}
