package org.websoso.WSSServer.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JWTUtilTest {

    private static final Long USER_ID = 42L;

    private final TestTokenFactory testTokenFactory = new TestTokenFactory();
    private final JWTUtil jwtUtil = new JWTUtil(new JwtKeyProvider(TestTokenFactory.TEST_SECRET));

    @DisplayName("유효한 Access Token은 VALID_ACCESS를 반환한다")
    @Test
    void validateJWT_validAccessToken_returnsValidAccess() {
        String token = testTokenFactory.createAccessToken(USER_ID);

        assertThat(jwtUtil.validateJWT(token)).isEqualTo(JwtValidationType.VALID_ACCESS);
    }

    @DisplayName("유효한 Refresh Token은 VALID_REFRESH를 반환한다")
    @Test
    void validateJWT_validRefreshToken_returnsValidRefresh() {
        String token = testTokenFactory.createRefreshToken(USER_ID);

        assertThat(jwtUtil.validateJWT(token)).isEqualTo(JwtValidationType.VALID_REFRESH);
    }

    @DisplayName("만료된 Access Token은 EXPIRED_ACCESS를 반환한다")
    @Test
    void validateJWT_expiredAccessToken_returnsExpiredAccess() {
        String token = testTokenFactory.createExpiredAccessToken(USER_ID);

        assertThat(jwtUtil.validateJWT(token)).isEqualTo(JwtValidationType.EXPIRED_ACCESS);
    }

    @DisplayName("만료된 Refresh Token은 EXPIRED_REFRESH를 반환한다")
    @Test
    void validateJWT_expiredRefreshToken_returnsExpiredRefresh() {
        String token = testTokenFactory.createExpiredRefreshToken(USER_ID);

        assertThat(jwtUtil.validateJWT(token)).isEqualTo(JwtValidationType.EXPIRED_REFRESH);
    }

    @DisplayName("서명이 유효해도 subject가 access/refresh가 아니면 UNSUPPORTED_SUBJECT를 반환한다")
    @Test
    void validateJWT_unknownSubjectToken_returnsUnsupportedSubject() {
        String token = testTokenFactory.createTokenWithSubject(USER_ID, "unknown");

        assertThat(jwtUtil.validateJWT(token)).isEqualTo(JwtValidationType.UNSUPPORTED_SUBJECT);
    }

    @DisplayName("만료된 토큰이어도 subject가 access/refresh가 아니면 UNSUPPORTED_SUBJECT를 반환한다")
    @Test
    void validateJWT_expiredUnknownSubjectToken_returnsUnsupportedSubject() {
        String token = testTokenFactory.createExpiredTokenWithSubject(USER_ID, "unknown");

        assertThat(jwtUtil.validateJWT(token)).isEqualTo(JwtValidationType.UNSUPPORTED_SUBJECT);
    }

    @DisplayName("다른 시크릿으로 서명된 Access Token은 INVALID_SIGNATURE를 반환한다")
    @Test
    void validateJWT_wrongSignatureAccessToken_returnsInvalidSignature() {
        String token = testTokenFactory.createAccessTokenWithInvalidSignature(USER_ID);

        assertThat(jwtUtil.validateJWT(token)).isEqualTo(JwtValidationType.INVALID_SIGNATURE);
    }

    @DisplayName("다른 시크릿으로 서명된 Refresh Token은 INVALID_SIGNATURE를 반환한다")
    @Test
    void validateJWT_wrongSignatureRefreshToken_returnsInvalidSignature() {
        String token = testTokenFactory.createRefreshTokenWithInvalidSignature(USER_ID);

        assertThat(jwtUtil.validateJWT(token)).isEqualTo(JwtValidationType.INVALID_SIGNATURE);
    }

    @DisplayName("형식이 깨진 토큰 문자열은 INVALID_TOKEN을 반환한다")
    @Test
    void validateJWT_malformedToken_returnsInvalidToken() {
        String token = "not-a-valid-jwt-token";

        assertThat(jwtUtil.validateJWT(token)).isEqualTo(JwtValidationType.INVALID_TOKEN);
    }

    @DisplayName("서명 없는(alg=none) 토큰은 UNSUPPORTED_TOKEN을 반환한다")
    @Test
    void validateJWT_unsignedToken_returnsUnsupportedToken() {
        String token = Jwts.builder()
                .setSubject(TokenType.ACCESS.getValue())
                .claim(JwtProvider.CLAIM_USER_ID, USER_ID)
                .compact();

        assertThat(jwtUtil.validateJWT(token)).isEqualTo(JwtValidationType.UNSUPPORTED_TOKEN);
    }

    @DisplayName("빈 토큰은 EMPTY_TOKEN을 반환한다")
    @Test
    void validateJWT_emptyToken_returnsEmptyToken() {
        assertThat(jwtUtil.validateJWT("")).isEqualTo(JwtValidationType.EMPTY_TOKEN);
    }

    @DisplayName("getUserIdFromJwt는 유효한 토큰에서 userId를 추출한다")
    @Test
    void getUserIdFromJwt_validToken_returnsUserId() {
        String token = testTokenFactory.createAccessToken(USER_ID);

        assertThat(jwtUtil.getUserIdFromJwt(token)).isEqualTo(USER_ID);
    }

    @DisplayName("getUserIdFromToken은 Bearer 접두사를 제거하고 만료된 토큰이어도 userId를 추출한다")
    @Test
    void getUserIdFromToken_expiredToken_stillReturnsUserId() {
        String token = testTokenFactory.createExpiredAccessToken(USER_ID);

        assertThat(jwtUtil.getUserIdFromToken("Bearer " + token)).isEqualTo(USER_ID);
    }

    @DisplayName("getUserIdFromToken은 파싱 불가능한 토큰이면 null을 반환한다")
    @Test
    void getUserIdFromToken_garbageToken_returnsNull() {
        assertThat(jwtUtil.getUserIdFromToken("Bearer not-a-valid-jwt-token")).isNull();
    }
}
