// CommentRequest.java
package board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CommentRequest {
    @NotBlank private String content;
}

// CommentResponse.java
