// dto/PostDto.java
package swing;


public record PostDto(
        Long   id,
        String title,
        String content,
        long   authorId,      // ★ 추가
        String author,        // ← authorName
        String createdAt,
        long   commentCount
) {}