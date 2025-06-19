package board.dto;

import board.entity.Post;
import lombok.Builder;

@Builder
public record PostResponseCnt(
        Long   id,
        String title,
        String content,
        Long   authorId,        // ★ 추가
        String author,
        String createdAt,
        long   commentCount
) {
    public static PostResponseCnt from(Post p, long commentCnt) {
        return PostResponseCnt.builder()
                .id(p.getId())
                .title(p.getTitle())
                .content(p.getContent())
                .authorId(p.getAuthor().getId())        // ★ 추가
                .author(p.getAuthor().getUsername())
                .createdAt(p.getCreatedAt().toString())
                .commentCount(commentCnt)
                .build();
    }
}
