package com.softserve.bookstoreapi.service;

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
import com.softserve.bookstoreapi.service.impl.AccountService;
import com.softserve.bookstoreapi.service.impl.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenFactory tokenFactory;

    @Mock
    private TokenSerializer tokenSerializer;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AccountService accountService;

    private UserRegisterRequestDTO validRegisterRequest;
    private LoginRequestDTO validLoginRequest;
    private Account testAccount;
    private Token testAccessToken;
    private Token testRefreshToken;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new UserRegisterRequestDTO(
                "testuser",
                "test@example.com",
                "password123"
        );

        validLoginRequest = new LoginRequestDTO(
                "test@example.com",
                "password123"
        );

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setUsername("testuser");
        testAccount.setEmail("test@example.com");
        testAccount.setPassword("encodedPassword");
        testAccount.setRole(UserRole.ROLE_CUSTOMER);
        testAccount.setBalance(BigDecimal.ZERO);

        testAccessToken = new Token(
                UUID.randomUUID(),
                "test@example.com",
                List.of("ROLE_CUSTOMER"),
                Instant.now(),
                Instant.now().plusSeconds(900)
        );

        testRefreshToken = new Token(
                UUID.randomUUID(),
                "test@example.com",
                List.of("ROLE_CUSTOMER"),
                Instant.now(),
                Instant.now().plusSeconds(604800)
        );
    }

    @Test
    void registerUser_Success() {
        when(accountRepository.existsByEmail(validRegisterRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(validRegisterRequest.password())).thenReturn("encodedPassword");
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        UserRegisterResponseDTO result = accountService.registerUser(validRegisterRequest);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(validRegisterRequest.email());
        assertThat(result.username()).isEqualTo(validRegisterRequest.username());
        assertThat(result.role()).isEqualTo(UserRole.ROLE_CUSTOMER);
        assertThat(result.balance()).isEqualTo(BigDecimal.ZERO);

        verify(accountRepository).existsByEmail(validRegisterRequest.email());
        verify(passwordEncoder).encode(validRegisterRequest.password());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsException() {
        when(accountRepository.existsByEmail(validRegisterRequest.email())).thenReturn(true);

        assertThatThrownBy(() -> accountService.registerUser(validRegisterRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("error.email.already.exists");

        verify(accountRepository).existsByEmail(validRegisterRequest.email());
        verify(passwordEncoder, never()).encode(anyString());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void login_Success_ReturnsTokens() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "test@example.com",
                "password123",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenFactory.createAccessToken(authentication)).thenReturn(testAccessToken);
        when(tokenFactory.createRefreshToken(authentication)).thenReturn(testRefreshToken);
        when(tokenSerializer.serialize(testAccessToken)).thenReturn("accessTokenString");
        when(tokenSerializer.serialize(testRefreshToken)).thenReturn("refreshTokenString");
        doNothing().when(refreshTokenService).saveRefreshToken(testRefreshToken);

        LoginResponseDTO result = accountService.login(validLoginRequest);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.accessToken()).isEqualTo("accessTokenString");
        assertThat(result.refreshToken()).isEqualTo("refreshTokenString");
        assertThat(result.roles()).contains("ROLE_CUSTOMER");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenFactory).createAccessToken(authentication);
        verify(tokenFactory).createRefreshToken(authentication);
        verify(tokenSerializer, times(2)).serialize(any(Token.class));
        verify(refreshTokenService).saveRefreshToken(testRefreshToken);
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> accountService.login(validLoginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenFactory, never()).createAccessToken(any());
        verify(tokenFactory, never()).createRefreshToken(any());
    }

    @Test
    void login_DisabledAccount_ThrowsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("Account is disabled"));

        assertThatThrownBy(() -> accountService.login(validLoginRequest))
                .isInstanceOf(DisabledException.class)
                .hasMessageContaining("Account is disabled");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenFactory, never()).createAccessToken(any());
    }

    @Test
    void findByEmail_Success() {
        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testAccount));

        Optional<Account> result = accountService.findByEmail("test@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");

        verify(accountRepository).findByEmail("test@example.com");
    }

    @Test
    void findByEmail_NotFound() {
        when(accountRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Optional<Account> result = accountService.findByEmail("nonexistent@example.com");

        assertThat(result).isEmpty();

        verify(accountRepository).findByEmail("nonexistent@example.com");
    }
}

