package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.logger.LoggerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Async
    public void sendPasswordReset(String to, String token) {
        log.info("Sending password reset email to: {}", LoggerUtils.obfuscate(to));
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("bookstoretest996@gmail.com");
        message.setTo(to);
        message.setSubject("Password Reset");
        message.setText("Click this link to reset your password: " +
                "https://localhost:3000/reset-password?token=" + token);
        
        try {
            mailSender.send(message);
            log.info("Password reset email successfully sent to: {}", LoggerUtils.obfuscate(to));
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}. Error: {}", 
                    LoggerUtils.obfuscate(to), e.getMessage(), e);
            throw e;
        }
    }
}
