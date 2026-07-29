package org.websoso.WSSServer.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

    private static final Long USER_ID = 42L;

    private final JwtKeyProvider jwtKeyProvider = new JwtKeyProvider(TestTokenFactory.TEST_SECRET);
    private final JwtProvider jwtProvider = new JwtProvider(jwtKeyProvider,
            TestTokenFactory.ACCESS_TOKEN_EXPIRATION, TestTokenFactory.REFRESH_TOKEN_EXPIRATION);

    @DisplayName("Access Token 발급 시 sub 클레임은 access, userId 클레임은 전달한 값이다")
    @Test
    void generateAccessToken_hasExpectedClaims() {
        CustomAuthenticationToken authentication = CustomAuthenticationToken.create(USER_ID);

        String token = jwtProvider.generateAccessToken(authentication);
        Claims claims = parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo("access");
        assertThat(claims.get(JwtProvider.CLAIM_USER_ID).toString()).isEqualTo(USER_ID.toString());
    }

    @DisplayName("Refresh Token 발급 시 sub 클레임은 refresh, userId 클레임은 전달한 값이다")
    @Test
    void generateRefreshToken_hasExpectedClaims() {
        CustomAuthenticationToken authentication = CustomAuthenticationToken.create(USER_ID);

        String token = jwtProvider.generateRefreshToken(authentication);
        Claims claims = parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo("refresh");
        assertThat(claims.get(JwtProvider.CLAIM_USER_ID).toString()).isEqualTo(USER_ID.toString());
    }

    @DisplayName("만료 시간이 지난 토큰도 정상적으로 발급되고 클레임에 만료시간이 반영된다")
    @Test
    void generateJWT_negativeExpiration_producesAlreadyExpiredToken() {
        CustomAuthenticationToken authentication = CustomAuthenticationToken.create(USER_ID);

        String token = jwtProvider.generateJWT(authentication, -1_000L, TokenType.ACCESS);
        Claims claims = parseClaimsIgnoringExpiration(token);

        assertThat(claims.getExpiration()).isBefore(new java.util.Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtKeyProvider.getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Claims parseClaimsIgnoringExpiration(String token) {
        try {
            return parseClaims(token);
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
