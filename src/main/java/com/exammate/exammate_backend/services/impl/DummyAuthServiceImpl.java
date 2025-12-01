package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.AuthResponse;
import com.exammate.exammate_backend.exception.BadRequestException;
import com.exammate.exammate_backend.models.User;
import com.exammate.exammate_backend.repositories.UserRepository;
import com.exammate.exammate_backend.security.JwtUtil;
import com.exammate.exammate_backend.services.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("dummyAuthServiceBean") // bean name different from the class name
@Transactional
@RequiredArgsConstructor
public class DummyAuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void requestOtp(String email) {
        // Intentionally do not generate or send an OTP. Ensure user exists for verification.
        userRepository.findByEmail(email).orElseGet(() -> createUserFromEmail(email));
    }

    @Override
    public AuthResponse verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String expectedOtp = isAdmin(user) ? "111111" : "000000";
        if (!expectedOtp.equals(otp)) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        String jwt = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());
        return AuthResponse.builder()
                .token(jwt)
                .build();
    }

    private boolean isAdmin(User user) {
        if (user.getRole() == null) return false;
        return "ADMIN".equalsIgnoreCase(user.getRole().toString());
    }

    private User createUserFromEmail(String email) {
        User user = User.builder()
                .email(email)
                .fullName("")
                .username(email)
                .password("")
                .build();
        return userRepository.save(user);
    }

}

