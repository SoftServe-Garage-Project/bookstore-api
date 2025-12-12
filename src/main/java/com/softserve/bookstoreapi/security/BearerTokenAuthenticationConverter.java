package com.softserve.bookstoreapi.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

@Slf4j
@RequiredArgsConstructor
public class BearerTokenAuthenticationConverter implements AuthenticationConverter {

    private final TokenDeserializer tokenDeserializer;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public Authentication convert(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            log.trace("No Bearer token found in Authorization header");
            return null;
        }

        String tokenValue = authorizationHeader.substring(BEARER_PREFIX.length()).trim();

        if (tokenValue.isEmpty()) {
            log.debug("Bearer token is empty");
            return null;
        }

        try {
            Token token = tokenDeserializer.deserialize(tokenValue);

            if (token == null) {
                log.warn("Token deserialization returned null");
                return null;
            }

            log.trace("Successfully converted Bearer token for user: {}", token.subject());
            return new PreAuthenticatedAuthenticationToken(token, tokenValue);

        } catch (Exception e) {
            log.warn("Failed to deserialize Bearer token: {}", e.getMessage());
            return null;
        }
    }
}

