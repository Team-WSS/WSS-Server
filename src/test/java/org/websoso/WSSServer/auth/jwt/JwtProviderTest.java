package org.websoso.WSSServer.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import java.util.HashSet;
import java.util.Set;
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

    @DisplayName("같은 사용자의 Refresh Token을 연속으로 발급해도 서로 다른 문자열이 나온다")
    @Test
    void generateRefreshToken_consecutiveCalls_produceDistinctTokens() {
        CustomAuthenticationToken authentication = CustomAuthenticationToken.create(USER_ID);

        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            tokens.add(jwtProvider.generateRefreshToken(authentication));
        }

        assertThat(tokens).hasSize(100);
    }

    @DisplayName("Refresh Token 발급 시 jti 클레임에 발급마다 다른 값이 담긴다")
    @Test
    void generateRefreshToken_hasUniqueJtiClaim() {
        CustomAuthenticationToken authentication = CustomAuthenticationToken.create(USER_ID);

        Claims first = parseClaims(jwtProvider.generateRefreshToken(authentication));
        Claims second = parseClaims(jwtProvider.generateRefreshToken(authentication));

        assertThat(first.getId()).isNotBlank();
        assertThat(second.getId()).isNotBlank();
        assertThat(second.getId()).isNotEqualTo(first.getId());
    }

    @DisplayName("Access Token 발급 시에는 jti 클레임을 추가하지 않는다")
    @Test
    void generateAccessToken_hasNoJtiClaim() {
        CustomAuthenticationToken authentication = CustomAuthenticationToken.create(USER_ID);

        Claims claims = parseClaims(jwtProvider.generateAccessToken(authentication));

        assertThat(claims.getId()).isNull();
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
