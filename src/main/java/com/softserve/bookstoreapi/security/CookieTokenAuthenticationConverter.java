package com.softserve.bookstoreapi.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class CookieTokenAuthenticationConverter implements AuthenticationConverter {

    private final TokenDeserializer tokenDeserializer;

    @Override
    public Authentication convert(HttpServletRequest request) {
        // Try to get token from cookie
        Optional<String> tokenValue = CookieUtil.getCookieValue(request, CookieUtil.ACCESS_TOKEN_COOKIE_NAME);

        if (tokenValue.isEmpty()) {
            log.trace("No access token found in cookies");
            return null;
        }

        String token = tokenValue.get();
        if (token.isEmpty()) {
            log.debug("Access token cookie is empty");
            return null;
        }

        try {
            Token deserializedToken = tokenDeserializer.deserialize(token);

            if (deserializedToken == null) {
                log.warn("Token deserialization returned null");
                return null;
            }

            log.trace("Successfully converted token from cookie for user: {}", deserializedToken.subject());
            return new PreAuthenticatedAuthenticationToken(deserializedToken, token);

        } catch (Exception e) {
            log.warn("Failed to deserialize token from cookie: {}", e.getMessage());
            return null;
        }
    }
}

