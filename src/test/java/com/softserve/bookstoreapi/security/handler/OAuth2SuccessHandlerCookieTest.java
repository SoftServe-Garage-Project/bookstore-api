package com.softserve.bookstoreapi.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.bookstoreapi.dto.LoginResponseDTO;
import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.model.enums.UserRole;
import com.softserve.bookstoreapi.security.Token;
import com.softserve.bookstoreapi.security.TokenFactory;
import com.softserve.bookstoreapi.security.TokenSerializer;
import com.softserve.bookstoreapi.service.impl.AccountService;
import com.softserve.bookstoreapi.service.impl.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for OAuth2SuccessHandler with HTTP-only cookie authentication.
 * Updated to verify cookies are set instead of tokens in JSON response.
 */
@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerCookieTest {

    @Mock
    private AccountService accountService;

    @Mock
    private TokenFactory tokenFactory;

    @Mock
    private TokenSerializer tokenSerializer;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void shouldHandleSuccessfulOAuth2LoginForExistingUser_SetsCookies() throws Exception {
        String email = "test@example.com";
        String name = "Test User";
        String provider = "google";

        OAuth2User oAuth2User = createOAuth2User(email, name);
        OAuth2AuthenticationToken authentication = createOAuth2Authentication(oAuth2User, provider);

        Account existingAccount = createAccount(1L, "Test User", email, UserRole.ROLE_CUSTOMER);

        Token accessToken = createToken(UUID.randomUUID(), email, List.of("ROLE_CUSTOMER"));
        Token refreshToken = createToken(UUID.randomUUID(), email, List.of("REFRESH_TOKEN"));

        String accessTokenString = "access-token-string";
        String refreshTokenString = "refresh-token-string";

        String expectedJson = "{\"email\":\"test@example.com\",\"roles\":[\"ROLE_CUSTOMER\"]}";

        when(accountService.findOrCreateOAuth2Account(email, name)).thenReturn(existingAccount);
        when(tokenFactory.createAccessToken(any(Authentication.class))).thenReturn(accessToken);
        when(tokenFactory.createRefreshToken(any(Authentication.class))).thenReturn(refreshToken);
        when(tokenSerializer.serialize(accessToken)).thenReturn(accessTokenString);
        when(tokenSerializer.serialize(refreshToken)).thenReturn(refreshTokenString);
        when(objectMapper.writeValueAsString(any(LoginResponseDTO.class))).thenReturn(expectedJson);

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        verify(accountService).findOrCreateOAuth2Account(email, name);
        verify(tokenFactory).createAccessToken(any(Authentication.class));
        verify(tokenFactory).createRefreshToken(any(Authentication.class));
        verify(tokenSerializer).serialize(accessToken);
        verify(tokenSerializer).serialize(refreshToken);
        verify(refreshTokenService).saveRefreshToken(refreshToken);

        // Verify cookies are set
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(2)).addCookie(cookieCaptor.capture());

        List<Cookie> cookies = cookieCaptor.getAllValues();
        assertThat(cookies).hasSize(2);

        Cookie accessTokenCookie = findCookie(cookies, "accessToken");
        assertThat(accessTokenCookie).isNotNull();
        assertThat(accessTokenCookie.getValue()).isEqualTo(accessTokenString);
        assertThat(accessTokenCookie.isHttpOnly()).isTrue();
        assertThat(accessTokenCookie.getSecure()).isTrue();

        Cookie refreshTokenCookie = findCookie(cookies, "refreshToken");
        assertThat(refreshTokenCookie).isNotNull();
        assertThat(refreshTokenCookie.getValue()).isEqualTo(refreshTokenString);
        assertThat(refreshTokenCookie.isHttpOnly()).isTrue();
        assertThat(refreshTokenCookie.getSecure()).isTrue();

        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setStatus(HttpServletResponse.SC_OK);

        printWriter.flush();
        String responseBody = stringWriter.toString();
        assertThat(responseBody).isEqualTo(expectedJson);
        assertThat(responseBody).doesNotContain("accessToken");
        assertThat(responseBody).doesNotContain("refreshToken");
    }

    @Test
    void shouldHandleSuccessfulOAuth2LoginForNewUser_SetsCookies() throws Exception {
        String email = "newuser@example.com";
        String name = "New User";
        String provider = "google";

        OAuth2User oAuth2User = createOAuth2User(email, name);
        OAuth2AuthenticationToken authentication = createOAuth2Authentication(oAuth2User, provider);

        Account savedAccount = createAccount(2L, name, email, UserRole.ROLE_CUSTOMER);

        Token accessToken = createToken(UUID.randomUUID(), email, List.of("ROLE_CUSTOMER"));
        Token refreshToken = createToken(UUID.randomUUID(), email, List.of("REFRESH_TOKEN"));

        String accessTokenString = "access-token-string";
        String refreshTokenString = "refresh-token-string";

        String expectedJson = "{\"email\":\"newuser@example.com\",\"roles\":[\"ROLE_CUSTOMER\"]}";

        when(accountService.findOrCreateOAuth2Account(email, name)).thenReturn(savedAccount);
        when(tokenFactory.createAccessToken(any(Authentication.class))).thenReturn(accessToken);
        when(tokenFactory.createRefreshToken(any(Authentication.class))).thenReturn(refreshToken);
        when(tokenSerializer.serialize(accessToken)).thenReturn(accessTokenString);
        when(tokenSerializer.serialize(refreshToken)).thenReturn(refreshTokenString);
        when(objectMapper.writeValueAsString(any(LoginResponseDTO.class))).thenReturn(expectedJson);

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        verify(accountService).findOrCreateOAuth2Account(email, name);
        verify(tokenFactory).createAccessToken(any(Authentication.class));
        verify(tokenFactory).createRefreshToken(any(Authentication.class));
        verify(refreshTokenService).saveRefreshToken(refreshToken);

        // Verify cookies are set
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(2)).addCookie(cookieCaptor.capture());

        List<Cookie> cookies = cookieCaptor.getAllValues();
        assertThat(cookies).hasSize(2);

        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setStatus(HttpServletResponse.SC_OK);

        printWriter.flush();
        String responseBody = stringWriter.toString();
        assertThat(responseBody).isEqualTo(expectedJson);
    }

    @Test
    void shouldHandleOAuth2LoginWithNullName_SetsCookies() throws Exception {
        String email = "noname@example.com";
        String provider = "google";

        OAuth2User oAuth2User = createOAuth2User(email, null);
        OAuth2AuthenticationToken authentication = createOAuth2Authentication(oAuth2User, provider);

        Account account = createAccount(3L, "noname", email, UserRole.ROLE_CUSTOMER);

        Token accessToken = createToken(UUID.randomUUID(), email, List.of("ROLE_CUSTOMER"));
        Token refreshToken = createToken(UUID.randomUUID(), email, List.of("REFRESH_TOKEN"));

        when(accountService.findOrCreateOAuth2Account(email, null)).thenReturn(account);
        when(tokenFactory.createAccessToken(any(Authentication.class))).thenReturn(accessToken);
        when(tokenFactory.createRefreshToken(any(Authentication.class))).thenReturn(refreshToken);
        when(tokenSerializer.serialize(any(Token.class))).thenReturn("token-string");
        when(objectMapper.writeValueAsString(any(LoginResponseDTO.class))).thenReturn("{}");

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        verify(accountService).findOrCreateOAuth2Account(email, null);

        // Verify cookies are set
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(2)).addCookie(cookieCaptor.capture());

        List<Cookie> cookies = cookieCaptor.getAllValues();
        assertThat(cookies).hasSize(2);
    }

    // Helper methods
    private OAuth2User createOAuth2User(String email, String name) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", email);
        if (name != null) {
            attributes.put("name", name);
        }
        attributes.put("sub", "123456789");

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub"
        );
    }

    private OAuth2AuthenticationToken createOAuth2Authentication(OAuth2User oAuth2User, String provider) {
        return new OAuth2AuthenticationToken(
                oAuth2User,
                oAuth2User.getAuthorities(),
                provider
        );
    }

    private Account createAccount(Long id, String username, String email, UserRole role) {
        Account account = new Account();
        account.setId(id);
        account.setUsername(username);
        account.setEmail(email);
        account.setRole(role);
        account.setBalance(BigDecimal.ZERO);
        return account;
    }

    private Token createToken(UUID tokenId, String subject, List<String> authorities) {
        Instant now = Instant.now();
        return new Token(tokenId, subject, authorities, now, now.plusSeconds(3600));
    }

    private Cookie findCookie(List<Cookie> cookies, String name) {
        return cookies.stream()
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
