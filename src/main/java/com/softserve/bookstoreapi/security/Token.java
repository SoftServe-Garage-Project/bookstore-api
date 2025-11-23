package com.softserve.bookstoreapi.security;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Token(UUID tokenId, String subject, List<String> authorities, Instant createdAt,
                    Instant expiresAt) {
}
