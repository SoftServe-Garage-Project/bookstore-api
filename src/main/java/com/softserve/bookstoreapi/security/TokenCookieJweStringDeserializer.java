package com.softserve.bookstoreapi.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.softserve.bookstoreapi.exception.InvalidJwtToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class TokenCookieJweStringDeserializer {

    private final JWEDecrypter jweDecrypter;

    public Token deserialize (String string) {
        if (string == null || string.trim().isEmpty()) {
            log.error("Token deserialization failed: token string is null or empty");
            throw new InvalidJwtToken("error.token.invalid");
        }

        try {
            var encryptedJWT = EncryptedJWT.parse(string);
            encryptedJWT.decrypt(this.jweDecrypter);
            var claimsSet = encryptedJWT.getJWTClaimsSet();
            return new Token(UUID.fromString(claimsSet.getJWTID()), claimsSet.getSubject(),
                    claimsSet.getStringListClaim("authorities"),
                    claimsSet.getIssueTime().toInstant(),
                    claimsSet.getExpirationTime().toInstant());
        } catch (ParseException | JOSEException exception) {
            log.error("Token deserialization failed: {}", exception.getMessage());
            throw new InvalidJwtToken("error.token.invalid");
        }
    }
}
