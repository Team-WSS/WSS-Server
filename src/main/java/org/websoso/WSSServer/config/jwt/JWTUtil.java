package org.websoso.WSSServer.config.jwt;

import static org.websoso.WSSServer.config.jwt.JwtProvider.CLAIM_USER_ID;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JWTUtil {

    private final JwtProvider jwtProvider;

    public Long getUserIdFromJwt(String token) {
        Claims claims = getClaim(token);
        return Long.valueOf(claims.get(CLAIM_USER_ID).toString());
    }

    public JwtValidationType validateJWT(String token) {
        try {
            final Claims claims = getClaim(token);
            String tokenType = claims.getSubject();
            if (tokenType.equals("access")) {
                return JwtValidationType.VALID_ACCESS;
            }
            return JwtValidationType.VALID_REFRESH;
        } catch (MalformedJwtException ex) {
            return JwtValidationType.INVALID_TOKEN;
        } catch (ExpiredJwtException ex) {
            String tokenType = ex.getClaims().getSubject();
            if (tokenType.equals("access")) {
                return JwtValidationType.EXPIRED_ACCESS;
            }
            return JwtValidationType.EXPIRED_REFRESH;
        } catch (UnsupportedJwtException ex) {
            return JwtValidationType.UNSUPPORTED_TOKEN;
        } catch (IllegalArgumentException ex) {
            return JwtValidationType.EMPTY_TOKEN;
        }
    }

    public Date getExpiration(String token) {
        return getClaim(token).getExpiration();
    }

    private Claims getClaim(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtProvider.getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
