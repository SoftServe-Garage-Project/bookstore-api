package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.service.impl.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendPasswordReset_Success() {
        String to = "test@example.com";
        String token = "valid.token.string";

        emailService.sendPasswordReset(to, token);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendPasswordReset_Failure() {
        String to = "test@example.com";
        String token = "valid.token.string";

        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(RuntimeException.class, () -> emailService.sendPasswordReset(to, token));
    }
}

