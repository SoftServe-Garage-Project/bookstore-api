package com.softserve.bookstoreapi.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import static org.mockito.Mockito.verify;

/**
 * Tests for OAuth2FailureHandler.
 * Verifies that OAuth2 authentication failures are properly handled with redirect.
 */
@ExtendWith(MockitoExtension.class)
class OAuth2FailureHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private OAuth2FailureHandler oAuth2FailureHandler;

    @Test
    void shouldRedirectToErrorPageOnAuthenticationFailure() throws Exception {
        AuthenticationException exception = new BadCredentialsException("Invalid credentials");

        oAuth2FailureHandler.onAuthenticationFailure(request, response, exception);

        verify(response).sendRedirect("https://localhost:3000/login?error=oauth2_failed");
    }

    @Test
    void shouldRedirectToErrorPageOnOAuth2AuthenticationException() throws Exception {
        OAuth2Error error = new OAuth2Error("invalid_grant", "Invalid authorization code", null);
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(error);

        oAuth2FailureHandler.onAuthenticationFailure(request, response, exception);

        verify(response).sendRedirect("https://localhost:3000/login?error=oauth2_failed");
    }

    @Test
    void shouldRedirectToErrorPageOnGenericException() throws Exception {
        AuthenticationException exception = new AuthenticationException("Generic auth error") {};

        oAuth2FailureHandler.onAuthenticationFailure(request, response, exception);

        verify(response).sendRedirect("https://localhost:3000/login?error=oauth2_failed");
    }
}
