package com.softserve.bookstoreapi.controllerAdvise;

import com.softserve.bookstoreapi.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    void handleValidationException_ReturnsValidationErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("loginRequest", "email", "Email is required");
        FieldError fieldError2 = new FieldError("loginRequest", "password", "Password is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Validation Failed");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.validation.failed");
        assertThat(response.getBody().getValidationErrors()).hasSize(2);
        assertThat(response.getBody().getValidationErrors()).containsEntry("email", "Email is required");
        assertThat(response.getBody().getValidationErrors()).containsEntry("password", "Password is required");
    }

    @Test
    void handleEmailAlreadyExists_ReturnsConflict() {
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("Email already exists", "test@example.com");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleEmailAlreadyExists(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Email Already Exists");
    }

    @Test
    void handleAccountNotFoundException_ReturnsNotFound() {
        AccountNotFoundException ex = new AccountNotFoundException("Account not found");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccountNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Account Not Found");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.account.not.found");
    }

    @Test
    void handleSessionAuthenticationException_ReturnsInternalServerError() {
        SessionAuthenticationException ex = new SessionAuthenticationException("Session failed");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleSessionAuthenticationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Authentication Session Failed");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.auth.session.failed");
    }

    @Test
    void handleTokenSerializationException_ReturnsInternalServerError() {
        TokenSerializationException ex = new TokenSerializationException("Failed to serialize");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTokenSerializationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Token Generation Failed");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.token.serialization.failed");
    }

    @Test
    void handleRefreshTokenStorageException_ReturnsInternalServerError() {
        RefreshTokenStorageException ex = new RefreshTokenStorageException("Storage failed");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleRefreshTokenStorageException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Failed to Store Refresh Token");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.refresh.token.storage.failed");
    }

    @Test
    void handleInvalidJwtToken_ReturnsUnauthorized() {
        InvalidJwtToken ex = new InvalidJwtToken("Invalid token");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidJwtToken(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Invalid Token");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.token.invalid");
    }

    @Test
    void handleAccessTokenExpired_ReturnsUnauthorized() {
        UUID tokenId = UUID.randomUUID();
        AccessTokenExpiredException ex = new AccessTokenExpiredException("Token expired", tokenId);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessTokenExpired(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Session Expired");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.token.access.expired");
    }

    @Test
    void handleTokenDeactivated_ReturnsUnauthorized() {
        UUID tokenId = UUID.randomUUID();
        TokenDeactivatedException ex = new TokenDeactivatedException("Token deactivated", tokenId);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTokenDeactivated(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Token Deactivated");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.token.deactivated");
    }

    @Test
    void handleRefreshTokenExpired_ReturnsUnauthorized() {
        UUID tokenId = UUID.randomUUID();
        RefreshTokenExpiredException ex = new RefreshTokenExpiredException("Token expired", tokenId);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleRefreshTokenExpired(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Refresh Token Expired");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.token.refresh.expired");
    }

    @Test
    void handleRefreshTokenInvalid_ReturnsUnauthorized() {
        UUID tokenId = UUID.randomUUID();
        RefreshTokenInvalidException ex = new RefreshTokenInvalidException("Token invalid", tokenId, "not_found");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleRefreshTokenInvalid(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Refresh Token Invalid");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.token.refresh.invalid.not_found");
    }

    @Test
    void handleInsufficientPermissions_ReturnsForbidden() {
        InsufficientPermissionsException ex = new InsufficientPermissionsException("No permissions");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInsufficientPermissions(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getError()).isEqualTo("Insufficient Permissions");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.insufficient.permissions");
    }

    @Test
    void handleBadCredentials_ReturnsUnauthorized() {
        BadCredentialsException ex = new BadCredentialsException("Invalid credentials");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Invalid Credentials");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.auth.invalid.credentials");
    }

    @Test
    void handleLockedException_ReturnsUnauthorized() {
        LockedException ex = new LockedException("Account locked");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleLockedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Account Locked");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.auth.account.locked");
    }

    @Test
    void handleDisabledException_ReturnsUnauthorized() {
        DisabledException ex = new DisabledException("Account disabled");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDisabledException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Account Disabled");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.auth.account.disabled");
    }

    @Test
    void handleAuthenticationException_ReturnsUnauthorized() {
        AuthenticationException ex = new AuthenticationException("Auth failed") {};

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Authentication Failed");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.auth.failed");
    }

    @Test
    void handleHttpMessageNotReadable_ReturnsBadRequest() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Malformed JSON");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleHttpMessageNotReadable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Malformed JSON Request");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.malformed.json");
    }

    @Test
    void handleIllegalArgumentException_ReturnsBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Invalid Request");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.invalid.argument");
    }

    @Test
    void handleDataAccessException_ReturnsServiceUnavailable() {
        DataAccessException ex = new DataAccessException("DB error") {};

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataAccessException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(503);
        assertThat(response.getBody().getError()).isEqualTo("Database Error");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.database.access");
    }

    @Test
    void handleGenericException_ReturnsInternalServerError() {
        Exception ex = new Exception("Unexpected error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getErrorCode()).isEqualTo("error.internal.server");
    }

    @Test
    void errorResponse_ContainsTimestamp() {
        Exception ex = new Exception("Test");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void errorResponse_WithValidationErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "error message");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(ex);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getValidationErrors()).isNotNull();
        assertThat(response.getBody().getValidationErrors()).isNotEmpty();
    }

    @Test
    void errorResponse_WithoutValidationErrors() {
        Exception ex = new Exception("Test");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getValidationErrors()).isNull();
    }
}

