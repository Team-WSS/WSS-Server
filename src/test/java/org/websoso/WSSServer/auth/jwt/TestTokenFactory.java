package org.websoso.WSSServer.auth.jwt;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;

public class TestTokenFactory {

    public static final String TEST_SECRET = "test-only-jwt-secret-never-used-in-production-0123456789";
    private static final String OTHER_SECRET = "a-completely-different-test-secret-not-matching-the-real-one-987654";
    public static final long ACCESS_TOKEN_EXPIRATION = 3_600_000L;
    public static final long REFRESH_TOKEN_EXPIRATION = 1_209_600_000L;

    private final JwtKeyProvider jwtKeyProvider;
    private final JwtProvider jwtProvider;

    public TestTokenFactory() {
        this(TEST_SECRET);
    }

    public TestTokenFactory(String secret) {
        this.jwtKeyProvider = new JwtKeyProvider(secret);
        this.jwtProvider = new JwtProvider(jwtKeyProvider, ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION);
    }

    public String createAccessToken(Long userId) {
        return jwtProvider.generateAccessToken(CustomAuthenticationToken.create(userId));
    }

    public String createRefreshToken(Long userId) {
        return jwtProvider.generateRefreshToken(CustomAuthenticationToken.create(userId));
    }

    public String createExpiredAccessToken(Long userId) {
        return jwtProvider.generateJWT(CustomAuthenticationToken.create(userId), -1_000L, TokenType.ACCESS);
    }

    public String createExpiredRefreshToken(Long userId) {
        return jwtProvider.generateJWT(CustomAuthenticationToken.create(userId), -1_000L, TokenType.REFRESH);
    }

    public String createAccessTokenWithInvalidSignature(Long userId) {
        return new TestTokenFactory(OTHER_SECRET).createAccessToken(userId);
    }

    public String createRefreshTokenWithInvalidSignature(Long userId) {
        return new TestTokenFactory(OTHER_SECRET).createRefreshToken(userId);
    }

    public String createTokenWithSubject(Long userId, String subject) {
        return signTokenWithSubject(userId, subject, ACCESS_TOKEN_EXPIRATION);
    }

    public String createExpiredTokenWithSubject(Long userId, String subject) {
        return signTokenWithSubject(userId, subject, -1_000L);
    }

    private String signTokenWithSubject(Long userId, String subject, long expirationTime) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationTime))
                .claim(JwtProvider.CLAIM_USER_ID, userId)
                .signWith(jwtKeyProvider.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
