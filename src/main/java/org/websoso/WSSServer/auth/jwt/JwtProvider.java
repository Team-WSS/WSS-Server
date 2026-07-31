package org.websoso.WSSServer.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    public static final String CLAIM_USER_ID = "userId";

    private final JwtKeyProvider jwtKeyProvider;
    private final Long accessTokenExpirationTime;
    private final Long refreshTokenExpirationTime;

    public JwtProvider(JwtKeyProvider jwtKeyProvider,
                       @Value("${jwt.expiration-time.access-token}") Long accessTokenExpirationTime,
                       @Value("${jwt.expiration-time.refresh-token}") Long refreshTokenExpirationTime) {
        this.jwtKeyProvider = jwtKeyProvider;
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }

    public String generateAccessToken(Authentication authentication) {
        return generateJWT(authentication, accessTokenExpirationTime, TokenType.ACCESS);
    }

    public String generateRefreshToken(Authentication authentication) {
        return generateJWT(authentication, refreshTokenExpirationTime, TokenType.REFRESH);
    }

    String generateJWT(Authentication authentication, Long expirationTime, TokenType tokenType) {
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(generateClaims(authentication, expirationTime, tokenType))
                .signWith(jwtKeyProvider.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims generateClaims(Authentication authentication, Long expirationTime, TokenType tokenType) {
        long now = System.currentTimeMillis();
        final Claims claims = Jwts.claims()
                .setSubject(tokenType.getValue())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationTime));
        claims.put(CLAIM_USER_ID, authentication.getPrincipal());

        // iat/exp는 초 단위로 잘리므로, 회전 대상인 Refresh Token은 jti로 발급마다 문자열 유일성을 보장한다.
        if (tokenType == TokenType.REFRESH) {
            claims.setId(UUID.randomUUID().toString());
        }

        return claims;
    }
}
