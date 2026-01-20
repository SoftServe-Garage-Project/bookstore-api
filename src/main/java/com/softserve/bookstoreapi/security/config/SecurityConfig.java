package com.softserve.bookstoreapi.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.softserve.bookstoreapi.repository.DeactivatedTokenRepository;
import com.softserve.bookstoreapi.security.BearerTokenAuthenticationConfigurer;
import com.softserve.bookstoreapi.security.TokenDeserializer;
import com.softserve.bookstoreapi.security.TokenFactory;
import com.softserve.bookstoreapi.security.TokenSerializer;
import com.softserve.bookstoreapi.security.handler.OAuth2FailureHandler;
import com.softserve.bookstoreapi.security.handler.OAuth2SuccessHandler;
import com.softserve.bookstoreapi.service.impl.AccountService;
import com.softserve.bookstoreapi.service.impl.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

import java.text.ParseException;
import java.time.Duration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, PublicUrlConfig publicUrlConfig,
                                                   BearerTokenAuthenticationConfigurer bearerTokenAuthenticationConfigurer,
                                                   OAuth2SuccessHandler oAuth2SuccessHandler,
                                                   OAuth2FailureHandler oAuth2FailureHandler) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests
                                .requestMatchers(publicUrlConfig.getRequestMatcher()).permitAll()
                                .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler))
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        bearerTokenAuthenticationConfigurer.configure(http);
        return http.build();
    }

    @Bean
    public static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ROLE_ADMIN").implies("ROLE_MANAGER")
                .role("ROLE_MANAGER").implies("ROLE_CUSTOMER")
                .build();
    }

    @Bean
    public static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }

    @Bean
    public static BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public static ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public TokenSerializer tokenSerializer(@Value("${jwt.cookie-token-key}") String cookieTokenKey) throws ParseException, KeyLengthException {
        return new TokenSerializer(new DirectEncrypter(
                OctetSequenceKey.parse(cookieTokenKey)
        ), JWEAlgorithm.DIR, EncryptionMethod.A128GCM);
    }

    @Bean
    public TokenFactory tokenFactory() {
        return new TokenFactory(Duration.ofMinutes(30), Duration.ofHours(8));
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            BCryptPasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(passwordEncoder);
        authenticationProvider.setUserDetailsService(userDetailsService);

        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public BearerTokenAuthenticationConfigurer bearerTokenAuthenticationConfigurer(
            TokenDeserializer deserializer,
            DeactivatedTokenRepository deactivatedTokenRepository,
            PublicUrlConfig publicUrlConfig) {

        return new BearerTokenAuthenticationConfigurer()
                .tokenDeserializer(deserializer)
                .deactivatedTokenRepository(deactivatedTokenRepository)
                .requestMatcher(new NegatedRequestMatcher(publicUrlConfig.getRequestMatcher()));
    }

    @Bean
    public TokenDeserializer tokenCookieJweStringDeserializer(
            @Value("${jwt.cookie-token-key}") String cookieTokenKey) throws Exception {
        return new TokenDeserializer(
                new DirectDecrypter(OctetSequenceKey.parse(cookieTokenKey))
        );
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler(
            AccountService accountService,
            TokenFactory tokenFactory,
            TokenSerializer tokenSerializer,
            RefreshTokenService refreshTokenService,
            ObjectMapper objectMapper) {
        return new OAuth2SuccessHandler(accountService, tokenFactory, tokenSerializer, refreshTokenService, objectMapper);
    }

    @Bean
    public OAuth2FailureHandler oAuth2FailureHandler() {
        return new OAuth2FailureHandler();
    }
}
