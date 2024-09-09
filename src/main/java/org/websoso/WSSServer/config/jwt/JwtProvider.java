package org.websoso.WSSServer.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private static final String CLAIM_USER_ID = "userId";

    @Value("${jwt.secret}")
    private String JWT_SECRET_KEY;

    @Value("${jwt.expiration-time.access-token}")
    private Long ACCESS_TOKEN_EXPIRATION_TIME;

    @Value("${jwt.expiration-time.refresh-token}")
    private Long REFRESH_TOKEN_EXPIRATION_TIME;

    @PostConstruct
    protected void init() {
        JWT_SECRET_KEY = Base64.getEncoder().encodeToString(JWT_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Authentication authentication) {
        return generateJWT(authentication, ACCESS_TOKEN_EXPIRATION_TIME, "access");
    }

    public String generateRefreshToken(Authentication authentication) {
        return generateJWT(authentication, REFRESH_TOKEN_EXPIRATION_TIME, "refresh");
    }

    public String generateJWT(Authentication authentication, Long expirationTime, String tokenType) {
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(generateClaims(authentication, expirationTime, tokenType))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims generateClaims(Authentication authentication, Long expirationTime, String tokenType) {
        long now = System.currentTimeMillis();
        final Claims claims = Jwts.claims()
                .setSubject(tokenType)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationTime));
        claims.put(CLAIM_USER_ID, authentication.getPrincipal());

        return claims;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes());
    }
}
