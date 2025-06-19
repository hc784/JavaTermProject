// SignupRequest.java
package board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SignupRequest {
    @NotBlank private String username;
    @NotBlank private String password;
}

// LoginRequest.java
