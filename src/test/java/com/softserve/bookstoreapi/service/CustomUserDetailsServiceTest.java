package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.exception.AccountNotFoundException;
import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.model.enums.Permissions;
import com.softserve.bookstoreapi.model.enums.UserRole;
import com.softserve.bookstoreapi.repository.AccountRepository;
import com.softserve.bookstoreapi.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setEmail("test@example.com");
        testAccount.setPassword("encodedPassword123");
        testAccount.setUsername("testuser");
        testAccount.setRole(UserRole.ROLE_CUSTOMER);
        testAccount.setBalance(BigDecimal.ZERO);
        testAccount.setPermissions(new ArrayList<>());
    }

    @Test
    void shouldLoadUserByExistingEmail() {
        String email = "test@example.com";
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(testAccount));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword123");
        verify(accountRepository, times(1)).findByEmail(email);
    }

    @Test
    void shouldThrowAccountNotFoundExceptionForNonExistentEmail() {
        String email = "nonexistent@example.com";
        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("user.not_found");

        verify(accountRepository, times(1)).findByEmail(email);
    }

    @Test
    void shouldMapAuthoritiesCorrectlyFromAccountWithSingleRole() {
        String email = "test@example.com";
        testAccount.setRole(UserRole.ROLE_CUSTOMER);
        testAccount.setPermissions(new ArrayList<>());
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(testAccount));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_CUSTOMER");
    }

    @Test
    void shouldMapAuthoritiesCorrectlyFromAccountWithRoleAndPermissions() {
        String email = "admin@example.com";
        testAccount.setEmail(email);
        testAccount.setRole(UserRole.ROLE_ADMIN);
        testAccount.setPermissions(List.of(
                Permissions.READ_BOOKS,
                Permissions.CREATE_BOOKS
        ));
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(testAccount));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertThat(userDetails.getAuthorities()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().contains("ROLE_ADMIN"))).isTrue();
    }

    @Test
    void shouldHandleAccountWithRoleAdminRole() {
        String email = "admin@example.com";
        testAccount.setEmail(email);
        testAccount.setRole(UserRole.ROLE_ADMIN);
        testAccount.setPermissions(new ArrayList<>());
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(testAccount));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getAuthorities()).isNotEmpty();
        assertThat(userDetails.getAuthorities())
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    @Test
    void shouldReturnUserDetailsWithCorrectPassword() {
        String email = "test@example.com";
        String encodedPassword = "$2a$10$encoded.password.hash";
        testAccount.setPassword(encodedPassword);
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(testAccount));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertThat(userDetails.getPassword()).isEqualTo(encodedPassword);
    }

    @Test
    void shouldHandleEmailWithSpecialCharacters() {
        String email = "user+test@example.com";
        testAccount.setEmail(email);
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(testAccount));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
    }

    @Test
    void shouldHandleAccountWithMultiplePermissions() {
        String email = "admin@example.com";
        testAccount.setEmail(email);
        testAccount.setRole(UserRole.ROLE_ADMIN);
        testAccount.setPermissions(List.of(
                Permissions.READ_BOOKS,
                Permissions.CREATE_BOOKS,
                Permissions.DELETE_BOOKS
        ));
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(testAccount));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertThat(userDetails.getAuthorities()).hasSizeGreaterThanOrEqualTo(1);
        verify(accountRepository, times(1)).findByEmail(email);
    }

    @Test
    void shouldCallRepositoryExactlyOnceWhenLoadingUser() {
        String email = "test@example.com";
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(testAccount));

        userDetailsService.loadUserByUsername(email);

        verify(accountRepository, times(1)).findByEmail(email);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void shouldHandleCaseSensitiveEmailLookup() {
        String lowercaseEmail = "test@example.com";
        String uppercaseEmail = "TEST@EXAMPLE.COM";
        when(accountRepository.findByEmail(lowercaseEmail)).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByEmail(uppercaseEmail)).thenReturn(Optional.empty());

        UserDetails userDetails = userDetailsService.loadUserByUsername(lowercaseEmail);

        assertThat(userDetails).isNotNull();

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(uppercaseEmail))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void shouldReturnUserInstanceFromSpringSecurity() {
        String email = "test@example.com";
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(testAccount));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertThat(userDetails).isInstanceOf(org.springframework.security.core.userdetails.User.class);
    }

    @Test
    void shouldPreserveEmailAsUsernameInUserDetails() {
        String email = "user@domain.com";
        testAccount.setEmail(email);
        testAccount.setUsername("different_username");
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(testAccount));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getUsername()).isNotEqualTo("different_username");
    }
}

