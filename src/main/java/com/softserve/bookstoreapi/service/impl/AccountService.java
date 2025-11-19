package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.dto.LoginRequestDTO;
import com.softserve.bookstoreapi.dto.LoginResponseDTO;
import com.softserve.bookstoreapi.dto.UserRegisterRequestDTO;
import com.softserve.bookstoreapi.dto.UserRegisterResponseDTO;
import com.softserve.bookstoreapi.exception.EmailAlreadyExistsException;
import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.model.enums.UserRole;
import com.softserve.bookstoreapi.repository.AccountRepository;
import com.softserve.bookstoreapi.security.TokenCookieSessionAuthenticationStrategy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.softserve.bookstoreapi.logger.LoggerUtils.obfuscate;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenCookieSessionAuthenticationStrategy tokenAuthenticationStrategy;

    public Optional<Account> findByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest, HttpServletRequest request, HttpServletResponse response) {
        log.debug("Login attempt for user: {}", obfuscate(loginRequest.email()));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        log.info("Successfully authenticated user: {}", obfuscate(authentication.getName()));

        tokenAuthenticationStrategy.onAuthentication(authentication, request, response);

        return buildLoginResponse(authentication);
    }

    private LoginResponseDTO buildLoginResponse(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new LoginResponseDTO(
                "Login successful",
                authentication.getName(),
                roles
        );
    }

    @Transactional
    public UserRegisterResponseDTO registerUser(UserRegisterRequestDTO requestDTO) {
        if (accountRepository.existsByEmail(requestDTO.email())) {
            throw new EmailAlreadyExistsException("error.email.already.exists", requestDTO.email());
        }

        Account account = new Account();
        account.setUsername(requestDTO.username());
        account.setEmail(requestDTO.email());
        account.setPassword(passwordEncoder.encode(requestDTO.password()));
        account.setRole(UserRole.ROLE_CUSTOMER);
        account.setBalance(BigDecimal.ZERO);

        Account savedAccount = accountRepository.save(account);
        log.info("Successful registration for email: {}", obfuscate(requestDTO.email()));

        return new UserRegisterResponseDTO(
                savedAccount.getId(),
                savedAccount.getUsername(),
                savedAccount.getEmail(),
                savedAccount.getRole(),
                savedAccount.getBalance()
        );
    }
}
