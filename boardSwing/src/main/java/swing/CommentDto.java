package swing;

public record CommentDto(Long id, String content,
        Long authorId, String author,
        String createdAt) { }
