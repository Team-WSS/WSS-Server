package org.websoso.WSSServer.auth.client;

import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.EMPTY_JWT;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.HEADER_PARSING_FAILED;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.INVALID_APPLE_TOKEN_FORMAT;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.JWT_VERIFICATION_FAILED;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.UNSUPPORTED_JWT_TYPE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.auth.client.dto.ApplePublicKeys;
import org.websoso.WSSServer.exception.exception.CustomAppleLoginException;

/**
 * Apple ID Token(idToken)의 헤더 파싱, 공개키 매칭, 서명 검증을 담당한다.
 * 서비스 JWT 발급(JwtProvider)과는 별개의 책임이다.
 */
@Component
@RequiredArgsConstructor
public class AppleIdTokenVerifier {

    private static final String IDENTITY_TOKEN_VALUE_DELIMITER = "\\.";
    private static final int HEADER_INDEX = 0;

    private final AppleClient appleClient;
    private final AppleKeyGenerator appleKeyGenerator;
    private final ObjectMapper objectMapper;

    public Claims verify(String appleIdToken) {
        Map<String, String> header = parseHeader(appleIdToken);
        ApplePublicKeys applePublicKeys = appleClient.getApplePublicKeys();
        PublicKey publicKey = appleKeyGenerator.generatePublicKey(header, applePublicKeys);
        return extractClaims(appleIdToken, publicKey);
    }

    private Map<String, String> parseHeader(String appleIdToken) {
        try {
            String encodedHeader = appleIdToken.split(IDENTITY_TOKEN_VALUE_DELIMITER)[HEADER_INDEX];
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

    private Claims extractClaims(String appleIdToken, PublicKey publicKey) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(appleIdToken)
                    .getBody();
        } catch (UnsupportedJwtException e) {
            throw new CustomAppleLoginException(UNSUPPORTED_JWT_TYPE, "unsupported JWT types");
        } catch (IllegalArgumentException e) {
            throw new CustomAppleLoginException(EMPTY_JWT, "empty jwt");
        } catch (JwtException e) {
            throw new CustomAppleLoginException(JWT_VERIFICATION_FAILED, "jwt validation or analysis failed");
        }
    }
}
