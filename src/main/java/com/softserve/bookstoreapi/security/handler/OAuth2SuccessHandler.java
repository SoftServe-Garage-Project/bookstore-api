package com.softserve.bookstoreapi.security.handler;

import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.security.CookieUtil;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

import static com.softserve.bookstoreapi.logger.LoggerUtils.obfuscate;

@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AccountService accountService;
    private final TokenFactory tokenFactory;
    private final TokenSerializer tokenSerializer;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
            String email = oauthUser.getAttribute("email");
            String name = oauthUser.getAttribute("name");

            Account account = accountService.findOrCreateOAuth2Account(email, name);

            log.info("OAuth2 login success for email: {}, provider: {}",
                    obfuscate(email),
                    ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId());

            TokenUser tokenUser = new TokenUser(
                    account.getEmail(),
                    "[PROTECTED]", // OAuth2 users don't use password authentication
                    account.getAuthorities(),
                    null
            );

            var auth = new UsernamePasswordAuthenticationToken(tokenUser, null, tokenUser.getAuthorities());

            Token accessToken = tokenFactory.createAccessToken(auth);
            String accessTokenString = tokenSerializer.serialize(accessToken);

            Token refreshToken = tokenFactory.createRefreshToken(auth);
            String refreshTokenString = tokenSerializer.serialize(refreshToken);
            refreshTokenService.saveRefreshToken(refreshToken);

            // Set tokens in HTTP-only cookies
            CookieUtil.setAuthenticationCookies(response, accessTokenString, refreshTokenString);

            // Redirect to frontend success page
            response.sendRedirect("http://localhost:3000/?auth_callback=true");
        } catch (Exception e) {
            log.error("OAuth2 authentication success handler failed", e);
            response.sendRedirect("https://localhost:3000/login?error=oauth2_failed");
        }
    }
}

