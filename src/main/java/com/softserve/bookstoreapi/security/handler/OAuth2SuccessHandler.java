package com.softserve.bookstoreapi.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.bookstoreapi.dto.LoginResponseDTO;
import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.security.Token;
import com.softserve.bookstoreapi.security.TokenFactory;
import com.softserve.bookstoreapi.security.TokenSerializer;
import com.softserve.bookstoreapi.security.TokenUser;
import com.softserve.bookstoreapi.service.impl.AccountService;
import com.softserve.bookstoreapi.service.impl.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.List;

import static com.softserve.bookstoreapi.logger.LoggerUtils.obfuscate;

@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AccountService accountService;
    private final TokenFactory tokenFactory;
    private final TokenSerializer tokenSerializer;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        Account account = accountService.findOrCreateOAuth2Account(email, name);

        log.info("OAuth2 login success for email: {}, provider: {}",
                obfuscate(email),
                ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId());

        TokenUser tokenUser = new TokenUser(
                account.getEmail(),
                account.getPassword(),
                account.getAuthorities(),
                null
        );

        var auth = new UsernamePasswordAuthenticationToken(tokenUser, null, tokenUser.getAuthorities());

        Token accessToken = tokenFactory.createAccessToken(auth);
        String accessTokenString = tokenSerializer.serialize(accessToken);

        Token refreshToken = tokenFactory.createRefreshToken(auth);
        String refreshTokenString = tokenSerializer.serialize(refreshToken);
        refreshTokenService.saveRefreshToken(refreshToken);

        List<String> roles = account.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        LoginResponseDTO loginResponse = new LoginResponseDTO(
                account.getEmail(),
                roles,
                accessTokenString,
                refreshTokenString
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(loginResponse));
    }
}

