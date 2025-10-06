package com.exammate.exammate_backend.repositories;

import com.exammate.exammate_backend.models.VerificationToken;
import com.exammate.exammate_backend.models.User;
import com.exammate.exammate_backend.models.VerificationToken.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByToken(String token);
    List<VerificationToken> findByUserAndType(User user, TokenType type);
    void deleteByUserAndType(User user, TokenType type);
}
