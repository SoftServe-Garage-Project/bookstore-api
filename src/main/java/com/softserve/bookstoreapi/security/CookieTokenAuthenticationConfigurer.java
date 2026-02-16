package com.softserve.bookstoreapi.security;

import com.softserve.bookstoreapi.repository.DeactivatedTokenRepository;
import com.softserve.bookstoreapi.service.impl.TokenAuthenticationUserDetailsService;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class CookieTokenAuthenticationConfigurer
        extends AbstractHttpConfigurer<CookieTokenAuthenticationConfigurer, HttpSecurity> {

    private DeactivatedTokenRepository deactivatedTokenRepository;
    private TokenDeserializer tokenDeserializer;
    private RequestMatcher requestMatcher;

    @Override
    public void init(HttpSecurity builder) {}

    @Override
    public void configure(HttpSecurity builder) {
        var authenticationProvider = new PreAuthenticatedAuthenticationProvider();
        authenticationProvider.setPreAuthenticatedUserDetailsService(
                new TokenAuthenticationUserDetailsService(deactivatedTokenRepository)
        );

        var authenticationManager = new ProviderManager(authenticationProvider);

        var bearerAuthenticationFilter = new AuthenticationFilter(
                authenticationManager,
                new CookieTokenAuthenticationConverter(this.tokenDeserializer)
        );

        bearerAuthenticationFilter.setRequestMatcher(requestMatcher);
        bearerAuthenticationFilter.setSuccessHandler((request, response, authentication) -> {});
        bearerAuthenticationFilter.setFailureHandler(new TokenCookieAuthenticationFailureHandler());

        builder.addFilterAfter(bearerAuthenticationFilter, CsrfFilter.class)
                .authenticationProvider(authenticationProvider);
    }

    public CookieTokenAuthenticationConfigurer tokenDeserializer(
            TokenDeserializer tokenDeserializer) {
        this.tokenDeserializer = tokenDeserializer;
        return this;
    }

    public CookieTokenAuthenticationConfigurer deactivatedTokenRepository(
            DeactivatedTokenRepository deactivatedTokenRepository) {
        this.deactivatedTokenRepository = deactivatedTokenRepository;
        return this;
    }

    public CookieTokenAuthenticationConfigurer requestMatcher(RequestMatcher requestMatcher) {
        this.requestMatcher = requestMatcher;
        return this;
    }
}

