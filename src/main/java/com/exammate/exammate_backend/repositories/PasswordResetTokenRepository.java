package com.exammate.exammate_backend.repositories;

import com.exammate.exammate_backend.models.PasswordResetToken;
import com.exammate.exammate_backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByToken(String token);
    void deleteAllByUser(User user);
}
