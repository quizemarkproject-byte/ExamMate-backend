package com.exammate.exammate_backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "verification_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false) // removed unique = true to allow non-unique OTP codes
    private String token;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    public VerificationToken(String token, Date expiryDate, TokenType type, User user) {
        this.token = token;
        this.expiryDate = expiryDate;
        this.type = type;
        this.user = user;
    }

    public static VerificationToken create(User user, TokenType type, int hoursValid) {
        String token = UUID.randomUUID().toString();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, hoursValid);
        return new VerificationToken(token, cal.getTime(), type, user);
    }

    public enum TokenType {
        EMAIL_VERIFICATION,
        PASSWORD_RESET,
        OTP_LOGIN // added for OTP-based authentication
    }
}
