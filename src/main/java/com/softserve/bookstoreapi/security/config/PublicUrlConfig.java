package com.softserve.bookstoreapi.security.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
@Getter
public class PublicUrlConfig {

    private final RequestMatcher requestMatcher;

    public PublicUrlConfig() {
        this.requestMatcher = new PublicUrlRequestMatcher();
    }

    private static class PublicUrlRequestMatcher implements RequestMatcher {
        private final AntPathMatcher pathMatcher = new AntPathMatcher();
        private final String[] publicUrls = {
                "/api/login",
                "/api/register",
                "/api/refresh",
                "/api/logout",
                "/error",
                "/oauth2/**",
                "/login/oauth2/**",
                "/api/forgot-password",
                "/api/reset-password"
        };

        @Override
        public boolean matches(HttpServletRequest request) {
            String requestPath = request.getRequestURI();
            String method = request.getMethod();

            // Allow only GET requests to /api/book endpoints
            if (requestPath.startsWith("/api/book") && "GET".equalsIgnoreCase(method)) {
                return true;
            }

            for (String pattern : publicUrls) {
                if (pathMatcher.match(pattern, requestPath)) {
                    return true;
                }
            }
            return false;
        }
    }
}

