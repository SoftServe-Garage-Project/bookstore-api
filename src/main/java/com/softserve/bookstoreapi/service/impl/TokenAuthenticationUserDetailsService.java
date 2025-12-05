package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.exception.AccessTokenExpiredException;
import com.softserve.bookstoreapi.exception.InvalidJwtToken;
import com.softserve.bookstoreapi.exception.TokenDeactivatedException;
import com.softserve.bookstoreapi.repository.DeactivatedTokenRepository;
import com.softserve.bookstoreapi.security.Token;
import com.softserve.bookstoreapi.security.TokenUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Instant;

@RequiredArgsConstructor
@Service
public class TokenAuthenticationUserDetailsService
        implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    private final DeactivatedTokenRepository deactivatedTokenRepository;

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authenticationToken)
            throws UsernameNotFoundException {
        if (!(authenticationToken.getPrincipal() instanceof Token token)) {
            throw new InvalidJwtToken("Invalid token principal type");
        }

        if (token.expiresAt().isBefore(Instant.now())) {
            throw new AccessTokenExpiredException("Access token has expired", token.tokenId());
        }

        if (deactivatedTokenRepository.existsById(token.tokenId())) {
            throw new TokenDeactivatedException("Token has been deactivated", token.tokenId());
        }


        return new TokenUser(
                token.subject(),
                "nopassword",
                true,
                true,
                true,
                true,
                token.authorities().stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList(),
                token
        );
    }
}
