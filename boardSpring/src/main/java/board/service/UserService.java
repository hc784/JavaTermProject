package board.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import board.dto.LoginRequest;
import board.dto.SignupRequest;
import board.entity.User;
import board.repository.UserRepository;

@Service @RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @Transactional
    public void signup(SignupRequest req) {
        if (userRepo.findByUsername(req.getUsername()).isPresent())
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");

        User user = User.builder()
                .username(req.getUsername())
                .passwordHash(encoder.encode(req.getPassword()))
                .build();
        userRepo.save(user);
    }

    public User login(LoginRequest req) {
        User user = userRepo.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        if (!encoder.matches(req.getPassword(), user.getPasswordHash()))
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");

        return user;   // JWT 없으므로 세션/토큰 관리 대신 프론트가 userId만 기억
    }
    
    public User findById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    
}
