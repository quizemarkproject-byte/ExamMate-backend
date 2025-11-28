package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.AuthResponse;
import com.exammate.exammate_backend.exception.BadRequestException;
import com.exammate.exammate_backend.models.User;
import com.exammate.exammate_backend.models.VerificationToken;
import com.exammate.exammate_backend.models.VerificationToken.TokenType;
import com.exammate.exammate_backend.repositories.UserRepository;
import com.exammate.exammate_backend.repositories.VerificationTokenRepository;
import com.exammate.exammate_backend.security.JwtUtil;
import com.exammate.exammate_backend.services.AuthService;
import com.exammate.exammate_backend.services.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    private static final int OTP_MINUTES_VALID = 5;

    @Override
    public void requestOtp(String email) {
        User user = userRepository.findByEmail(email).orElseGet(() -> createUserFromEmail(email));
        // remove existing OTP tokens
        verificationTokenRepository.deleteByUserAndType(user, TokenType.OTP_LOGIN);
        String otp = generateOtp();
        VerificationToken otpToken = buildOtpToken(user, otp, OTP_MINUTES_VALID);
        verificationTokenRepository.save(otpToken);
        sendOtpEmail(user.getEmail(), otp);
    }

    @Override
    public AuthResponse verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
        List<VerificationToken> tokens = verificationTokenRepository.findByUserAndType(user, TokenType.OTP_LOGIN);
        VerificationToken match = tokens.stream().filter(t -> t.getToken().equals(otp)).findFirst()
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP"));
        if (match.getExpiryDate().before(new Date())) {
            verificationTokenRepository.deleteByUserAndType(user, TokenType.OTP_LOGIN);
            throw new BadRequestException("Invalid or expired OTP");
        }
        verificationTokenRepository.deleteByUserAndType(user, TokenType.OTP_LOGIN); // consume
        String jwt = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());
        return AuthResponse.builder()
                .token(jwt)
                .build();
    }

    private User createUserFromEmail(String email) {
        User user = User.builder()
                .email(email)
                // supply non-null values for NOT NULL columns
                .fullName("")
                .username(email)
                .password("")
                .build();
        return userRepository.save(user);
    }

    private String generateOtp() {
        int n = new Random().nextInt(900_000) + 100_000; // 6-digit
        return String.valueOf(n);
    }

    private VerificationToken buildOtpToken(User user, String otp, int minutesValid) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, minutesValid);
        return new VerificationToken(otp, cal.getTime(), TokenType.OTP_LOGIN, user);
    }

    private void sendOtpEmail(String email, String otp) {
        String subject = "Your ExamMate login code";
        String body = String.format("Your one-time login code is: %s\nIt expires in %d minutes.", otp, OTP_MINUTES_VALID);
        emailService.sendEmail(email, subject, body);
    }

    private String generateRandomString(int len) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
        return sb.toString();
    }
}
