package org.websoso.WSSServer.oauth2.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.websoso.WSSServer.dto.auth.AppleLoginRequest;
import org.websoso.WSSServer.dto.auth.ApplePublicKey;
import org.websoso.WSSServer.dto.auth.ApplePublicKeys;
import org.websoso.WSSServer.dto.auth.AuthResponse;
import org.websoso.WSSServer.service.UserService;

@Service
@RequiredArgsConstructor
public class AppleService { // TODO : 커스텀 예외로 수정

    private static final String APPLE_PREFIX = "apple";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_SUB = "sub";
    private static final String IDENTITY_TOKEN_VALUE_DELIMITER = "\\.";
    private static final int HEADER_INDEX = 0;
    private static final String SIGN_ALGORITHM_HEADER = "alg";
    private static final String KEY_ID_HEADER = "kid";
    private static final int POSITIVE_SIGN_NUMBER = 1;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Value("${apple.public-keys-url}")
    private String APPLE_PUBLIC_KEYS_URL;

    public AuthResponse getUserInfoFromApple(final AppleLoginRequest request) {
        final String appleToken = request.appleToken();
        final Map<String, String> appleTokenHeader = parseAppleTokenHeader(appleToken);
        final ApplePublicKeys applePublicKeys = getApplePublicKeys();
        final PublicKey publicKey = generatePublicKeyFromHeaders(appleTokenHeader, applePublicKeys);
        final Claims claims = extractClaims(appleToken, publicKey);

        final String email = claims.get(CLAIM_EMAIL, String.class);
        final String userIdentifier = claims.get(CLAIM_SUB, String.class);
        String customSocialId = APPLE_PREFIX + "_" + userIdentifier;
        String defaultNickname = APPLE_PREFIX.charAt(0) + "*" + userIdentifier.substring(7, 15);

        return userService.authenticateWithApple(customSocialId, email, defaultNickname);
    }

    private Map<String, String> parseAppleTokenHeader(final String appleToken) {
        try {
            final String encodedHeader = appleToken.split(IDENTITY_TOKEN_VALUE_DELIMITER)[HEADER_INDEX];
            final String decodedHeader = new String(Base64.getUrlDecoder().decode(encodedHeader));
            return objectMapper.readValue(decodedHeader, Map.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException("appleToken 값이 jwt 형식인지, 값이 정상적인지 확인해주세요.");
        } catch (JsonProcessingException e) {
            throw new RuntimeException("디코드된 헤더를 Map 형태로 분류할 수 없습니다. 헤더를 확인해주세요.");
        }
    }

    private ApplePublicKeys getApplePublicKeys() {
        RestClient restClient = RestClient.create();
        return restClient.get()
                .uri(APPLE_PUBLIC_KEYS_URL)
                .retrieve()
                .body(ApplePublicKeys.class);
    }

    private PublicKey generatePublicKeyFromHeaders(final Map<String, String> headers,
                                                   final ApplePublicKeys publicKeys) {
        final ApplePublicKey applePublicKey = publicKeys.getMatchingKey(
                headers.get(SIGN_ALGORITHM_HEADER),
                headers.get(KEY_ID_HEADER)
        );
        return generatePublicKey(applePublicKey);
    }

    private PublicKey generatePublicKey(final ApplePublicKey applePublicKey) {
        final byte[] nBytes = Base64.getUrlDecoder().decode(applePublicKey.n());
        final byte[] eBytes = Base64.getUrlDecoder().decode(applePublicKey.e());

        final BigInteger n = new BigInteger(POSITIVE_SIGN_NUMBER, nBytes);
        final BigInteger e = new BigInteger(POSITIVE_SIGN_NUMBER, eBytes);
        final RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(n, e);

        try {
            final KeyFactory keyFactory = KeyFactory.getInstance(applePublicKey.kty());
            return keyFactory.generatePublic(rsaPublicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new RuntimeException("잘못된 애플 키"); // TODO : 커스텀 예외로 수정
        }
    }

    private Claims extractClaims(final String appleToken, final PublicKey publicKey) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(appleToken)
                    .getBody();
        } catch (UnsupportedJwtException e) {
            throw new UnsupportedJwtException("지원되지 않는 jwt 타입");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("비어있는 jwt");
        } catch (JwtException e) {
            throw new JwtException("jwt 검증 or 분석 오류");
        }
    }
}
