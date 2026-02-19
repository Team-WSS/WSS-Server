package org.websoso.WSSServer.config.jwt;

import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.EMPTY_JWT;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.HEADER_PARSING_FAILED;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.INVALID_APPLE_TOKEN_FORMAT;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.JWT_VERIFICATION_FAILED;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.UNSUPPORTED_JWT_TYPE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.exception.exception.CustomAppleLoginException;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    protected static final String CLAIM_USER_ID = "userId";

    private static final String IDENTITY_TOKEN_VALUE_DELIMITER = "\\.";
    private static final int HEADER_INDEX = 0;

    private final ObjectMapper objectMapper;

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

    public Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return extractUserId(claims);

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return extractUserId(e.getClaims());
        } catch (Exception e) {
            return null;
        }
    }

    public Claims extractClaims(String appleToken, PublicKey publicKey) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(appleToken)
                    .getBody();
        } catch (UnsupportedJwtException e) {
            throw new CustomAppleLoginException(UNSUPPORTED_JWT_TYPE, "unsupported JWT types");
        } catch (IllegalArgumentException e) {
            throw new CustomAppleLoginException(EMPTY_JWT, "empty jwt");
        } catch (JwtException e) {
            throw new CustomAppleLoginException(JWT_VERIFICATION_FAILED, "jwt validation or analysis failed");
        }
    }

    public Map<String, String> parseAppleTokenHeader(String appleToken) {
        try {
            String encodedHeader = appleToken.split(IDENTITY_TOKEN_VALUE_DELIMITER)[HEADER_INDEX];
            String decodedHeader = new String(Base64.getUrlDecoder().decode(encodedHeader));
            return objectMapper.readValue(decodedHeader, Map.class);
        } catch (JsonMappingException e) {
            throw new CustomAppleLoginException(INVALID_APPLE_TOKEN_FORMAT,
                    "make sure the idToken value is in jwt format and that the value is valid");
        } catch (JsonProcessingException e) {
            throw new CustomAppleLoginException(HEADER_PARSING_FAILED,
                    "the decoded header cannot be classified as a Map, please check the header");
        }
    }

    private Long extractUserId(Claims claims) {
        Object userId = claims.get(CLAIM_USER_ID);
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
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

    protected SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes());
    }
}
