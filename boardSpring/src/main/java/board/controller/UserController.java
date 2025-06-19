package board.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import board.dto.LoginRequest;
import board.dto.LoginResponse;
import board.dto.SignupRequest;
import board.entity.User;
import board.service.UserService;

@RestController @RequestMapping("/api/users") @RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public String signup(@RequestBody @Valid SignupRequest req) {
        userService.signup(req);
        return "회원가입 성공";
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest req) {
        User user = userService.login(req);
        return new LoginResponse(user.getId(), user.getUsername(), user.isAdmin());
    }
}
