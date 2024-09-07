package org.websoso.WSSServer.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
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

    private static final String USER_ID = "userId";
    private static final Long TOKEN_EXPIRATION_TIME = 6 * 30 * 24 * 60 * 60 * 1000L;

    @Value("${jwt.secret}")
    private String JWT_SECRET;

    @PostConstruct
    protected void init() {
        JWT_SECRET = Base64.getEncoder().encodeToString(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String generateJWT(Authentication authentication) {
        final Date now = new Date();

        final Claims claims = Jwts.claims()
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + TOKEN_EXPIRATION_TIME));

        claims.put(USER_ID, authentication.getPrincipal());

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        String encodedKey = Base64.getEncoder().encodeToString(JWT_SECRET.getBytes());
        return Keys.hmacShaKeyFor(
                encodedKey.getBytes());
    }

    public JwtValidationType validateToken(String token) {
        try {
            final Claims claims = getClaim(token);
            return JwtValidationType.VALID_TOKEN;
        } catch (MalformedJwtException ex) {
            return JwtValidationType.INVALID_TOKEN;
        } catch (ExpiredJwtException ex) {
            return JwtValidationType.EXPIRED_TOKEN;
        } catch (UnsupportedJwtException ex) {
            return JwtValidationType.UNSUPPORTED_TOKEN;
        } catch (IllegalArgumentException ex) {
            return JwtValidationType.EMPTY_TOKEN;
        }
    }

    private Claims getClaim(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserFromJwt(String token) {
        Claims claims = getClaim(token);
        return Long.valueOf(claims.get(USER_ID).toString());
    }

}
