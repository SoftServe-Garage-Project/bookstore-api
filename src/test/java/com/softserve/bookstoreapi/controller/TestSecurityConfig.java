package com.softserve.bookstoreapi.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// NOTE: This is a duplicate file. Please use the one in com.softserve.bookstoreapi.config package instead
// This file exists only to prevent compilation errors during refactoring

@TestConfiguration
public class TestSecurityConfigDuplicate {

    @Bean
    public SecurityFilterChain testSecurityFilterChain2(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder testPasswordEncoder2() {
        return new BCryptPasswordEncoder();
    }
}

