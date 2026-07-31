package org.websoso.WSSServer.auth.jwt;

import static org.websoso.WSSServer.auth.jwt.JwtProvider.CLAIM_USER_ID;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JWTUtil {

    private final JwtKeyProvider jwtKeyProvider;

    public Long getUserIdFromJwt(String token) {
        Claims claims = getClaim(token);
        return Long.valueOf(claims.get(CLAIM_USER_ID).toString());
    }

    /**
     * Bearer 접두사를 제거하고, 만료된 토큰이어도 클레임에서 userId를 추출한다.
     * 그 외 파싱 실패는 모두 null을 반환한다. deprecated /auth/apple/sync 전용.
     */
    public Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            Claims claims = getClaim(token);
            return extractUserId(claims);
        } catch (ExpiredJwtException e) {
            return extractUserId(e.getClaims());
        } catch (Exception e) {
            return null;
        }
    }

    public JwtValidationType validateJWT(String token) {
        try {
            final Claims claims = getClaim(token);
            final TokenType tokenType = TokenType.from(claims.getSubject());
            if (tokenType == TokenType.ACCESS) {
                return JwtValidationType.VALID_ACCESS;
            }
            if (tokenType == TokenType.REFRESH) {
                return JwtValidationType.VALID_REFRESH;
            }
            return JwtValidationType.UNSUPPORTED_SUBJECT;
        } catch (SignatureException ex) {
            return JwtValidationType.INVALID_SIGNATURE;
        } catch (MalformedJwtException ex) {
            return JwtValidationType.INVALID_TOKEN;
        } catch (ExpiredJwtException ex) {
            final TokenType tokenType = TokenType.from(ex.getClaims().getSubject());
            if (tokenType == TokenType.ACCESS) {
                return JwtValidationType.EXPIRED_ACCESS;
            }
            if (tokenType == TokenType.REFRESH) {
                return JwtValidationType.EXPIRED_REFRESH;
            }
            return JwtValidationType.UNSUPPORTED_SUBJECT;
        } catch (UnsupportedJwtException ex) {
            return JwtValidationType.UNSUPPORTED_TOKEN;
        } catch (IllegalArgumentException ex) {
            return JwtValidationType.EMPTY_TOKEN;
        }
    }

    private Long extractUserId(Claims claims) {
        Object userId = claims.get(CLAIM_USER_ID);
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    private Claims getClaim(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtKeyProvider.getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
