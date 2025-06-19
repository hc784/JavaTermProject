// advice/RestExceptionHandler.java
package board.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)          // 400
    public String illegalArg(IllegalArgumentException ex) {
        return ex.getMessage();                      // "이미 존재하는 사용자명입니다."
    }
}