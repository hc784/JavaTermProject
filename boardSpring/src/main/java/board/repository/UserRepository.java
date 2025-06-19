package board.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import board.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
