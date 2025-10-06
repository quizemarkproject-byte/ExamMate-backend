package com.exammate.exammate_backend.services;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
