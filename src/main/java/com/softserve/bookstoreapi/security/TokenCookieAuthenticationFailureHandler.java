package com.softserve.bookstoreapi.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class TokenCookieAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        log.debug("Access token authentication failed: {}", exception.getMessage());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String jsonResponse = """
                {
                    "timestamp": "%s",
                    "status": 401,
                    "error": "Unauthorized",
                    "errorCode": "error.token.authentication.failed",
                    "message": "Authentication failed"
                }
                """.formatted(java.time.LocalDateTime.now());

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}



