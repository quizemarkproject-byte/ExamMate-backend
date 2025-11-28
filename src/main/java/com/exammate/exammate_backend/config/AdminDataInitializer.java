package com.exammate.exammate_backend.config;

import com.exammate.exammate_backend.models.Role;
import com.exammate.exammate_backend.models.User;
import com.exammate.exammate_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(AdminDataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${EMAIL_USERNAME:}")
    private String adminEmail;

    @Value("${EMAIL_PASSWORD:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank()) {
            logger.info("EMAIL_USERNAME not set — skipping admin user creation");
            return;
        }

        Optional<User> existing = userRepository.findByEmail(adminEmail);
        if (existing.isPresent()) {
            User u = existing.get();
            boolean changed = false;
            if (u.getRole() != Role.ADMIN) {
                u.setRole(Role.ADMIN);
                changed = true;
            }
            if (!u.isEnabled()) {
                u.setEnabled(true);
                changed = true;
            }
            if (adminPassword != null && !adminPassword.isBlank()) {
                String encoded = passwordEncoder.encode(adminPassword);
                u.setPassword(encoded);
                changed = true;
            }
            if (changed) {
                userRepository.save(u);
                logger.info("Updated existing admin user: {}", adminEmail);
            } else {
                logger.info("Admin user already exists and is up to date: {}", adminEmail);
            }
            return;
        }

        // create new admin user
        String pwd = adminPassword;
        if (pwd == null || pwd.isBlank()) {
            logger.warn("EMAIL_PASSWORD not set — creating admin with empty password (not recommended)");
            pwd = "";
        }

        User admin = User.builder()
                .email(adminEmail)
                .fullName("Admin")
                .username(adminEmail)
                .password(passwordEncoder.encode(pwd))
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        userRepository.save(admin);
        logger.info("Created admin user: {}", adminEmail);
    }
}
