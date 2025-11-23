package com.softserve.bookstoreapi.controllerAdvise;

import com.softserve.bookstoreapi.exception.AccessTokenExpiredException;
import com.softserve.bookstoreapi.exception.AccountNotFoundException;
import com.softserve.bookstoreapi.exception.EmailAlreadyExistsException;
import com.softserve.bookstoreapi.exception.InsufficientPermissionsException;
import com.softserve.bookstoreapi.exception.InvalidJwtToken;
import com.softserve.bookstoreapi.exception.RefreshTokenExpiredException;
import com.softserve.bookstoreapi.exception.RefreshTokenInvalidException;
import com.softserve.bookstoreapi.exception.RefreshTokenStorageException;
import com.softserve.bookstoreapi.exception.TokenDeactivatedException;
import com.softserve.bookstoreapi.exception.TokenSerializationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.softserve.bookstoreapi.logger.LoggerUtils.obfuscate;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed: {}", errors);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", "error.validation.failed", errors);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        log.warn("Registration failed - email already exists: {}", obfuscate(ex.getEmail()));
        return buildErrorResponse(HttpStatus.CONFLICT, "Email Already Exists", ex.getMessage());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException ex) {
        log.warn("Account not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Account Not Found", "error.account.not.found");
    }

    @ExceptionHandler(SessionAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleSessionAuthenticationException(SessionAuthenticationException ex) {
        log.error("Session authentication failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Authentication Session Failed", "error.auth.session.failed");
    }

    @ExceptionHandler(TokenSerializationException.class)
    public ResponseEntity<ErrorResponse> handleTokenSerializationException(TokenSerializationException ex) {
        log.error("Failed to serialize token: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Token Generation Failed", "error.token.serialization.failed");
    }

    @ExceptionHandler(RefreshTokenStorageException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenStorageException(RefreshTokenStorageException ex) {
        log.error("Refresh token storage failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to Store Refresh Token", "error.refresh.token.storage.failed");
    }

    @ExceptionHandler(InvalidJwtToken.class)
    public ResponseEntity<ErrorResponse> handleInvalidJwtToken(InvalidJwtToken ex) {
        log.warn("Invalid JWT token: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid Token", "error.token.invalid");
    }

    @ExceptionHandler(AccessTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleAccessTokenExpired(AccessTokenExpiredException ex) {
        log.debug("Access token expired. Token ID: {}", ex.getTokenId());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Session Expired", "error.token.access.expired");
    }

    @ExceptionHandler(TokenDeactivatedException.class)
    public ResponseEntity<ErrorResponse> handleTokenDeactivated(TokenDeactivatedException ex) {
        log.debug("Token has been deactivated. Token ID: {}", ex.getTokenId());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Token Deactivated", "error.token.deactivated");
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenExpired(RefreshTokenExpiredException ex) {
        log.info("Refresh token expired. Token ID: {}", ex.getTokenId());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Refresh Token Expired", "error.token.refresh.expired");
    }

    @ExceptionHandler(RefreshTokenInvalidException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenInvalid(RefreshTokenInvalidException ex) {
        log.warn("Refresh token invalid. Token ID: {}, Reason: {}", ex.getTokenId(), ex.getReason());
        String errorCode = "error.token.refresh.invalid." + ex.getReason();
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Refresh Token Invalid", errorCode);
    }

    @ExceptionHandler(InsufficientPermissionsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPermissions(InsufficientPermissionsException ex) {
        log.warn("Insufficient permissions: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Insufficient Permissions", "error.insufficient.permissions");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication failed - invalid credentials");
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid Credentials", "error.auth.invalid.credentials");
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLockedException(LockedException ex) {
        log.warn("Authentication failed - account locked");
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Account Locked", "error.auth.account.locked");
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(DisabledException ex) {
        log.warn("Authentication failed - account disabled");
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Account Disabled", "error.auth.account.disabled");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication Failed", "error.auth.failed");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Request", "error.invalid.argument");
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex) {
        log.error("Database access error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Database Error", "error.database.access");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "error.internal.server");
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String errorCode) {
        return buildErrorResponse(status, error, errorCode, null);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String errorCode,
                                                             Map<String, String> validationErrors) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .errorCode(errorCode)
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }
}


