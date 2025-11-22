package com.softserve.bookstoreapi.security.config;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.softserve.bookstoreapi.repository.DeactivatedTokenRepository;
import com.softserve.bookstoreapi.security.BearerTokenAuthenticationConfigurer;
import com.softserve.bookstoreapi.security.TokenCookieJweStringDeserializer;
import com.softserve.bookstoreapi.security.TokenFactory;
import com.softserve.bookstoreapi.security.TokenSerializer;
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
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, PublicUrlConfig publicUrlConfig,
                                                   BearerTokenAuthenticationConfigurer bearerTokenAuthenticationConfigurer) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests
                                .requestMatchers(publicUrlConfig.getRequestMatcher()).permitAll()
                                .anyRequest().authenticated())
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
    public TokenSerializer tokenSerializer(@Value("${jwt.cookie-token-key}") String cookieTokenKey) throws ParseException, KeyLengthException {
        return new TokenSerializer(new DirectEncrypter(
                OctetSequenceKey.parse(cookieTokenKey)
        ), JWEAlgorithm.DIR, EncryptionMethod.A128GCM);
    }

    @Bean
    public TokenFactory tokenFactory() {
//        return new TokenFactory(Duration.ofMinutes(30), Duration.ofHours(8));
        return new TokenFactory(Duration.ofMinutes(1), Duration.ofMinutes(5));
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
            TokenCookieJweStringDeserializer deserializer,
            DeactivatedTokenRepository deactivatedTokenRepository,
            PublicUrlConfig publicUrlConfig) {

        return new BearerTokenAuthenticationConfigurer()
                .tokenDeserializer(deserializer)
                .deactivatedTokenRepository(deactivatedTokenRepository)
                .requestMatcher(new NegatedRequestMatcher(publicUrlConfig.getRequestMatcher()));
    }

    @Bean
    public TokenCookieJweStringDeserializer tokenCookieJweStringDeserializer(
            @Value("${jwt.cookie-token-key}") String cookieTokenKey) throws Exception {
        return new TokenCookieJweStringDeserializer(
                new DirectDecrypter(OctetSequenceKey.parse(cookieTokenKey))
        );
    }
}
