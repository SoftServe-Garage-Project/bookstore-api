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
                "/error"
        };

        @Override
        public boolean matches(HttpServletRequest request) {
            String requestPath = request.getRequestURI();
            for (String pattern : publicUrls) {
                if (pathMatcher.match(pattern, requestPath)) {
                    return true;
                }
            }
            return false;
        }
    }
}

