package com.softserve.bookstoreapi.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jca.JWEJCAContext;
import com.softserve.bookstoreapi.exception.TokenSerializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class TokenSerializerTest {

    private TokenSerializer tokenSerializer;
    private Token testAccessToken;
    private Token testRefreshToken;
    private SecretKey secretKey;

    private static final JWEAlgorithm JWE_ALGORITHM = JWEAlgorithm.DIR;
    private static final EncryptionMethod ENCRYPTION_METHOD = EncryptionMethod.A128GCM;

    @BeforeEach
    void setUp() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128, new SecureRandom());
        secretKey = keyGenerator.generateKey();

        JWEEncrypter jweEncrypter = new DirectEncrypter(secretKey);
        tokenSerializer = new TokenSerializer(jweEncrypter, JWE_ALGORITHM, ENCRYPTION_METHOD);

        testAccessToken = new Token(
                UUID.randomUUID(),
                "test@example.com",
                List.of("ROLE_USER", "ROLE_CUSTOMER"),
                Instant.now(),
                Instant.now().plusSeconds(900)
        );

        testRefreshToken = new Token(
                UUID.randomUUID(),
                "test@example.com",
                List.of("REFRESH_TOKEN"),
                Instant.now(),
                Instant.now().plusSeconds(604800)
        );
    }

    @Test
    void shouldSerializeAccessTokenSuccessfully() {
        String serializedToken = tokenSerializer.serialize(testAccessToken);

        assertThat(serializedToken).isNotNull();
        assertThat(serializedToken).isNotEmpty();
        assertThat(serializedToken.split("\\.")).hasSizeGreaterThanOrEqualTo(5);
    }

    @Test
    void shouldSerializeRefreshTokenSuccessfully() {
        String serializedToken = tokenSerializer.serialize(testRefreshToken);

        assertThat(serializedToken).isNotNull();
        assertThat(serializedToken).isNotEmpty();
        assertThat(serializedToken.split("\\.")).hasSizeGreaterThanOrEqualTo(5);
    }

    @Test
    void shouldThrowTokenSerializationExceptionWhenEncryptionFails() throws Exception {
        JWEEncrypter brokenEncrypter = new JWEEncrypter() {
            @Override
            public JWEJCAContext getJCAContext() {
                return null;
            }

            @Override
            public JWECryptoParts encrypt(JWEHeader header, byte[] clearText, byte[] aad) throws JOSEException {
                throw new JOSEException("Encryption failed");
            }

            @Override
            public java.util.Set<JWEAlgorithm> supportedJWEAlgorithms() {
                return java.util.Set.of(JWE_ALGORITHM);
            }

            @Override
            public java.util.Set<EncryptionMethod> supportedEncryptionMethods() {
                return java.util.Set.of(ENCRYPTION_METHOD);
            }
        };
        TokenSerializer brokenSerializer = new TokenSerializer(brokenEncrypter, JWE_ALGORITHM, ENCRYPTION_METHOD);

        assertThatThrownBy(() -> brokenSerializer.serialize(testAccessToken))
                .isInstanceOf(TokenSerializationException.class)
                .hasMessage("Failed to encrypt JWT token")
                .hasCauseInstanceOf(JOSEException.class);
    }

    @Test
    void shouldVerifyJweFormatContainsFivePartsSeparatedByDots() {
        String serializedToken = tokenSerializer.serialize(testAccessToken);

        assertThat(serializedToken.split("\\.")).hasSize(5);
    }

    @Test
    void shouldIncludeTokenIdInSerializedToken() throws Exception {
        String serializedToken = tokenSerializer.serialize(testAccessToken);

        assertThat(serializedToken).isNotNull();
        JWEObject jweObject = JWEObject.parse(serializedToken);
        jweObject.decrypt(new DirectDecrypter(secretKey));
        assertThat(jweObject.getHeader().getKeyID()).isEqualTo(testAccessToken.tokenId().toString());
    }

    @Test
    void shouldSerializeTokenWithAllRequiredClaims() throws Exception {
        String serializedToken = tokenSerializer.serialize(testAccessToken);

        assertThat(serializedToken).isNotNull();
        JWEObject jweObject = JWEObject.parse(serializedToken);
        jweObject.decrypt(new DirectDecrypter(secretKey));
        String payload = jweObject.getPayload().toString();

        assertThat(payload).contains("\"sub\":\"test@example.com\"");
        assertThat(payload).contains("ROLE_USER");
        assertThat(payload).contains("ROLE_CUSTOMER");
    }

    @Test
    void shouldSerializeMultipleTokensIndependently() {
        String serializedToken1 = tokenSerializer.serialize(testAccessToken);
        String serializedToken2 = tokenSerializer.serialize(testRefreshToken);

        assertThat(serializedToken1).isNotNull().isNotEmpty();
        assertThat(serializedToken2).isNotNull().isNotEmpty();
        assertThat(serializedToken1).isNotEqualTo(serializedToken2);
    }

    @Test
    void shouldHandleTokenWithSpecialCharactersInSubject() {
        Token tokenWithSpecialChars = new Token(
                UUID.randomUUID(),
                "user+test@example.com",
                List.of("ROLE_USER"),
                Instant.now(),
                Instant.now().plusSeconds(900)
        );

        String serializedToken = tokenSerializer.serialize(tokenWithSpecialChars);

        assertThat(serializedToken).isNotNull().isNotEmpty();
    }

    @Test
    void shouldHandleTokenWithMultipleAuthorities() {
        Token tokenWithMultipleAuthorities = new Token(
                UUID.randomUUID(),
                "admin@example.com",
                List.of("ROLE_ADMIN", "ROLE_USER", "ROLE_MODERATOR", "READ_PRIVILEGE", "WRITE_PRIVILEGE"),
                Instant.now(),
                Instant.now().plusSeconds(900)
        );

        String serializedToken = tokenSerializer.serialize(tokenWithMultipleAuthorities);

        assertThat(serializedToken).isNotNull().isNotEmpty();
    }

    @Test
    void shouldPropagateExceptionMessageWhenSerializationFails() throws Exception {
        String expectedErrorMessage = "Custom encryption error";
        JWEEncrypter brokenEncrypter = new JWEEncrypter() {
            @Override
            public JWEJCAContext getJCAContext() {
                return null;
            }

            @Override
            public JWECryptoParts encrypt(JWEHeader header, byte[] clearText, byte[] aad) throws JOSEException {
                throw new JOSEException(expectedErrorMessage);
            }

            @Override
            public java.util.Set<JWEAlgorithm> supportedJWEAlgorithms() {
                return java.util.Set.of(JWE_ALGORITHM);
            }

            @Override
            public java.util.Set<EncryptionMethod> supportedEncryptionMethods() {
                return java.util.Set.of(ENCRYPTION_METHOD);
            }
        };
        TokenSerializer brokenSerializer = new TokenSerializer(brokenEncrypter, JWE_ALGORITHM, ENCRYPTION_METHOD);

        assertThatThrownBy(() -> brokenSerializer.serialize(testAccessToken))
                .isInstanceOf(TokenSerializationException.class)
                .hasMessage("Failed to encrypt JWT token")
                .hasCauseInstanceOf(JOSEException.class)
                .cause()
                .hasMessage(expectedErrorMessage);
    }
}

