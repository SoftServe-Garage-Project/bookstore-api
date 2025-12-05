package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.exception.AccountNotFoundException;
import com.softserve.bookstoreapi.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("user.not_found"));

        return new User(
                account.getEmail(),
                account.getPassword(),
                account.getAuthorities()
        );
    }
}

