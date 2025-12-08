package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.dto.LoginRequestDTO;
import com.softserve.bookstoreapi.dto.LoginResponseDTO;
import com.softserve.bookstoreapi.dto.UserRegisterRequestDTO;
import com.softserve.bookstoreapi.dto.UserRegisterResponseDTO;
import com.softserve.bookstoreapi.exception.EmailAlreadyExistsException;
import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.model.enums.UserRole;
import com.softserve.bookstoreapi.repository.AccountRepository;
import com.softserve.bookstoreapi.security.Token;
import com.softserve.bookstoreapi.security.TokenFactory;
import com.softserve.bookstoreapi.security.TokenSerializer;
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
    private final TokenFactory tokenFactory;
    private final TokenSerializer tokenSerializer;
    private final RefreshTokenService refreshTokenService;

    public Optional<Account> findByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        log.debug("Login attempt for user: {}", obfuscate(loginRequest.email()));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        log.info("User successfully authenticated: {}", obfuscate(authentication.getName()));

        Token accessToken = tokenFactory.createAccessToken(authentication);
        String accessTokenString = tokenSerializer.serialize(accessToken);

        Token refreshToken = tokenFactory.createRefreshToken(authentication);
        String refreshTokenString = tokenSerializer.serialize(refreshToken);
        
        refreshTokenService.saveRefreshToken(refreshToken);

        return buildLoginResponse(authentication, accessTokenString, refreshTokenString);
    }


    private LoginResponseDTO buildLoginResponse(Authentication authentication, String accessToken, String refreshToken) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new LoginResponseDTO(
                authentication.getName(),
                roles,
                accessToken,
                refreshToken
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

    @Transactional
    public Account save(Account account) {
        return accountRepository.save(account);
    }

    @Transactional
    public Account findOrCreateOAuth2Account(String email, String name) {
        return accountRepository.findByEmail(email)
                .orElseGet(() -> {
                    Account newAccount = new Account();
                    String username = name != null ? name : (email != null ? email.split("@")[0] : "user");
                    newAccount.setUsername(username);
                    newAccount.setEmail(email);
                    newAccount.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                    newAccount.setRole(UserRole.ROLE_CUSTOMER);
                    newAccount.setBalance(BigDecimal.ZERO);

                    Account savedAccount = accountRepository.save(newAccount);
                    log.info("Created new OAuth2 account for email: {}", obfuscate(email));
                    return savedAccount;
                });
    }
}
