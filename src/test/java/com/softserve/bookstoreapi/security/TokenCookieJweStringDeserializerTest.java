package com.softserve.bookstoreapi.security;

import com.nimbusds.jose.JWEDecrypter;
import com.softserve.bookstoreapi.exception.InvalidJwtToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenCookieJweStringDeserializerTest {

    @Mock
    private JWEDecrypter jweDecrypter;

    private TokenDeserializer deserializer;

    private static final String MALFORMED_TOKEN = "this.is.not.a.valid.jwt";
    private static final String EMPTY_TOKEN = "";
    private static final String NULL_TOKEN = null;

    @BeforeEach
    void setUp() {
        deserializer = new TokenDeserializer(jweDecrypter);
    }

    @Test
    void shouldThrowInvalidJwtTokenForMalformedTokenString() {
        // When & Then
        assertThatThrownBy(() -> deserializer.deserialize(MALFORMED_TOKEN))
                .isInstanceOf(InvalidJwtToken.class)
                .hasMessage("error.token.invalid");
    }

    @Test
    void shouldThrowInvalidJwtTokenForEmptyTokenString() {
        assertThatThrownBy(() -> deserializer.deserialize(EMPTY_TOKEN))
                .isInstanceOf(InvalidJwtToken.class)
                .hasMessage("error.token.invalid");
    }

    @Test
    void shouldThrowInvalidJwtTokenForNullTokenString() {
        assertThatThrownBy(() -> deserializer.deserialize(NULL_TOKEN))
                .isInstanceOf(InvalidJwtToken.class)
                .hasMessage("error.token.invalid");
    }

    @Test
    void shouldThrowInvalidJwtTokenWhenParseExceptionOccurs() {
        String invalidTokenFormat = "not..valid..jwt..format..here";

        assertThatThrownBy(() -> deserializer.deserialize(invalidTokenFormat))
                .isInstanceOf(InvalidJwtToken.class)
                .hasMessage("error.token.invalid");
    }

    @Test
    void shouldThrowInvalidJwtTokenWhenDecryptionFails() {
        String tokenWithDecryptionError = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4R0NNIn0..invalid.cipher.text";

        assertThatThrownBy(() -> deserializer.deserialize(tokenWithDecryptionError))
                .isInstanceOf(InvalidJwtToken.class)
                .hasMessage("error.token.invalid");
    }

    @Test
    void shouldThrowInvalidJwtTokenForTokenWithInvalidSignature() {
        String tamperedToken = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4R0NNIn0.invalid.signature.here.data";

        assertThatThrownBy(() -> deserializer.deserialize(tamperedToken))
                .isInstanceOf(InvalidJwtToken.class)
                .hasMessage("error.token.invalid");
    }

    @Test
    void shouldThrowInvalidJwtTokenForTokenWithMissingParts() {
        String incompleteParts = "eyJhbGciOiJkaXIifQ.eyJzdWIiOiJ0ZXN0In0";

        assertThatThrownBy(() -> deserializer.deserialize(incompleteParts))
                .isInstanceOf(InvalidJwtToken.class)
                .hasMessage("error.token.invalid");
    }

    @Test
    void shouldThrowInvalidJwtTokenForTokenWithTooManyParts() {
        String tooManyParts = "part1.part2.part3.part4.part5.part6.part7";

        assertThatThrownBy(() -> deserializer.deserialize(tooManyParts))
                .isInstanceOf(InvalidJwtToken.class)
                .hasMessage("error.token.invalid");
    }

    @Test
    void shouldThrowInvalidJwtTokenForTokenWithInvalidBase64Encoding() {
        String invalidBase64 = "!!!invalid!!!.base64.encoding.here.test";

        assertThatThrownBy(() -> deserializer.deserialize(invalidBase64))
                .isInstanceOf(InvalidJwtToken.class)
                .hasMessage("error.token.invalid");
    }

    @Test
    void shouldThrowInvalidJwtTokenWithConsistentErrorCode() {
        String[] invalidTokens = {
                MALFORMED_TOKEN,
                EMPTY_TOKEN,
                "eyJ.invalid",
                "a.b.c.d.e.f",
                "!!!.###.$$$"
        };

        for (String invalidToken : invalidTokens) {
            assertThatThrownBy(() -> deserializer.deserialize(invalidToken))
                    .isInstanceOf(InvalidJwtToken.class)
                    .hasMessage("error.token.invalid");
        }
    }

    @Test
    void shouldHandleWhitespaceInTokenString() {
        String tokenWithWhitespace = "  eyJhbGci.test.token  ";

        assertThatThrownBy(() -> deserializer.deserialize(tokenWithWhitespace))
                .isInstanceOf(InvalidJwtToken.class)
                .hasMessage("error.token.invalid");
    }

    @Test
    void shouldThrowInvalidJwtTokenForTokenWithSpecialCharacters() {
        String tokenWithSpecialChars = "token@#$%.with^&*().special!chars";

        assertThatThrownBy(() -> deserializer.deserialize(tokenWithSpecialChars))
                .isInstanceOf(InvalidJwtToken.class)
                .hasMessage("error.token.invalid");
    }

    @Test
    void shouldThrowInvalidJwtTokenInsteadOfOtherExceptions() {
        String[] problematicTokens = {
                null,
                "",
                "   ",
                "single",
                "two.parts",
                "!!!.###.$$$",
                MALFORMED_TOKEN
        };

        for (String token : problematicTokens) {
            assertThatThrownBy(() -> deserializer.deserialize(token))
                    .isInstanceOf(InvalidJwtToken.class)
                    .hasMessage("error.token.invalid");
        }
    }
}

