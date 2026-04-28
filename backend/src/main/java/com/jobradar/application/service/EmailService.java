package com.jobradar.application.service;

import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    public EmailService(JavaMailSender mailSender) { this.mailSender = mailSender; }

    public void sendVerificationEmail(String to, String token) {

        String link = "http://localhost:5173/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("JobRadar Email Verification");
        message.setText(
                "Welcome to JobRadar!\n\n" +
                        "Please confirm your email:\n" +
                        link + "\n\n" +
                        "Valid for 24 hours."
        );

        mailSender.send(message);
    }
}
