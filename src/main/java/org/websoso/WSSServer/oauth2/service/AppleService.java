package org.websoso.WSSServer.oauth2.service;

import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.CLIENT_SECRET_CREATION_FAILED;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.EMPTY_JWT;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.HEADER_PARSING_FAILED;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.INVALID_APPLE_KEY;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.INVALID_APPLE_TOKEN_FORMAT;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.JWT_VERIFICATION_FAILED;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.PRIVATE_KEY_READ_FAILED;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.TOKEN_REQUEST_FAILED;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.UNSUPPORTED_JWT_TYPE;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.USER_APPLE_REFRESH_TOKEN_NOT_FOUND;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.websoso.WSSServer.config.jwt.JwtProvider;
import org.websoso.WSSServer.config.jwt.UserAuthentication;
import org.websoso.WSSServer.domain.RefreshToken;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserAppleToken;
import org.websoso.WSSServer.dto.auth.AppleLoginRequest;
import org.websoso.WSSServer.dto.auth.ApplePublicKey;
import org.websoso.WSSServer.dto.auth.ApplePublicKeys;
import org.websoso.WSSServer.dto.auth.AppleTokenResponse;
import org.websoso.WSSServer.dto.auth.AuthResponse;
import org.websoso.WSSServer.exception.exception.CustomAppleLoginException;
import org.websoso.WSSServer.repository.RefreshTokenRepository;
import org.websoso.WSSServer.repository.UserAppleTokenRepository;
import org.websoso.WSSServer.repository.UserRepository;
import org.websoso.WSSServer.service.MessageService;

@Transactional
@Service
@RequiredArgsConstructor
public class AppleService {

    private static final String APPLE_PREFIX = "apple";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_SUB = "sub";
    private static final String IDENTITY_TOKEN_VALUE_DELIMITER = "\\.";
    private static final int HEADER_INDEX = 0;
    private static final String SIGN_ALGORITHM_HEADER = "alg";
    private static final String KEY_ID_HEADER = "kid";
    private static final int POSITIVE_SIGN_NUMBER = 1;
    private final ObjectMapper objectMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final UserAppleTokenRepository userAppleTokenRepository;
    private final JwtProvider jwtProvider;
    private final MessageService messageService;

    @Value("${apple.public-keys-url}")
    private String applePublicKeysUrl;

    @Value("${apple.expiration-time}")
    private long tokenExpirationTime;

    @Value("${apple.team-id}")
    private String appleTeamId;

    @Value("${apple.key.id}")
    private String appleLoginKey;

    @Value("${apple.client-id}")
    private String appleClientId;

    @Value("${apple.redirect-url}")
    private String appleRedirectUrl;

    @Value("${apple.key.path}")
    private String appleKeyPath;

    @Value("${apple.iss}")
    private String appleAuthUrl;

    public AuthResponse getUserInfoFromApple(AppleLoginRequest request) {
        String appleToken = request.idToken();
        Map<String, String> appleTokenHeader = parseAppleTokenHeader(appleToken);
        ApplePublicKeys applePublicKeys = getApplePublicKeys();
        PublicKey publicKey = generatePublicKeyFromHeaders(appleTokenHeader, applePublicKeys);
        Claims claims = extractClaims(appleToken, publicKey);

        AppleTokenResponse appleTokenResponse = requestAppleToken(request.authorizationCode(), createClientSecret());

        String email = claims.get(CLAIM_EMAIL, String.class);
        String userIdentifier = claims.get(CLAIM_SUB, String.class);
        String customSocialId = APPLE_PREFIX + "_" + userIdentifier;
        String defaultNickname = APPLE_PREFIX.charAt(0) + "*" + userIdentifier.substring(7, 15);

        return authenticate(customSocialId, email, defaultNickname, appleTokenResponse.getRefreshToken());
    }

    public void unlinkFromApple(User user) {
        UserAppleToken userAppleToken = userAppleTokenRepository.findByUser(user).orElseThrow(
                () -> new CustomAppleLoginException(USER_APPLE_REFRESH_TOKEN_NOT_FOUND,
                        "cannot find the user Apple refresh token"));

        RestClient restClient = RestClient.create();
        restClient.post()
                .uri(appleAuthUrl + "/auth/revoke")
                .headers(headers -> headers.add("Content-Type", "application/x-www-form-urlencoded"))
                .body(createUserRevokeParams(createClientSecret(), userAppleToken.getAppleRefreshToken()))
                .retrieve()
                .body(String.class);

        userAppleTokenRepository.delete(userAppleToken);
    }

    private Map<String, String> parseAppleTokenHeader(String appleToken) {
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

    private ApplePublicKeys getApplePublicKeys() {
        RestClient restClient = RestClient.create();
        return restClient.get()
                .uri(applePublicKeysUrl)
                .retrieve()
                .body(ApplePublicKeys.class);
    }

    private PublicKey generatePublicKeyFromHeaders(Map<String, String> headers,
                                                   ApplePublicKeys publicKeys) {
        ApplePublicKey applePublicKey = publicKeys.getMatchingKey(
                headers.get(SIGN_ALGORITHM_HEADER),
                headers.get(KEY_ID_HEADER)
        );
        return generatePublicKey(applePublicKey);
    }

    private PublicKey generatePublicKey(ApplePublicKey applePublicKey) {
        byte[] nBytes = Base64.getUrlDecoder().decode(applePublicKey.n());
        byte[] eBytes = Base64.getUrlDecoder().decode(applePublicKey.e());

        BigInteger n = new BigInteger(POSITIVE_SIGN_NUMBER, nBytes);
        BigInteger e = new BigInteger(POSITIVE_SIGN_NUMBER, eBytes);
        RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(n, e);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(applePublicKey.kty());
            return keyFactory.generatePublic(rsaPublicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new CustomAppleLoginException(INVALID_APPLE_KEY, "invalid apple key");
        }
    }

    private Claims extractClaims(String appleToken, PublicKey publicKey) {
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

    private String createClientSecret() {
        try {
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(appleLoginKey).build();
            JWTClaimsSet claimsSet = buildJwtClaimsSet();

            SignedJWT jwt = new SignedJWT(header, claimsSet);
            signJwt(jwt);

            return jwt.serialize();
        } catch (Exception e) {
            throw new CustomAppleLoginException(CLIENT_SECRET_CREATION_FAILED, "failed to generate client secret");
        }
    }

    private JWTClaimsSet buildJwtClaimsSet() {
        Date now = new Date();
        JWTClaimsSet claimsSet = new JWTClaimsSet();

        claimsSet.setIssuer(appleTeamId);
        claimsSet.setIssueTime(now);
        claimsSet.setExpirationTime(new Date(now.getTime() + tokenExpirationTime));
        claimsSet.setAudience(appleAuthUrl);
        claimsSet.setSubject(appleClientId);

        return claimsSet;
    }

    private void signJwt(SignedJWT jwt) {
        try {
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(readPrivateKey(appleKeyPath));
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            ECPrivateKey ecPrivateKey = (ECPrivateKey) keyFactory.generatePrivate(spec);
            JWSSigner signer = new ECDSASigner(ecPrivateKey.getS());
            jwt.sign(signer);
        } catch (Exception e) {
            throw new CustomAppleLoginException(CLIENT_SECRET_CREATION_FAILED, "failed to create client secret");
        }
    }

    private byte[] readPrivateKey(String keyPath) {
        Resource resource = new ClassPathResource(keyPath);
        try (PemReader pemReader = new PemReader(new InputStreamReader(resource.getInputStream()))) {
            PemObject pemObject = pemReader.readPemObject();
            return pemObject.getContent();
        } catch (IOException e) {
            throw new CustomAppleLoginException(PRIVATE_KEY_READ_FAILED, "failed to read private key");
        }
    }

    private AppleTokenResponse requestAppleToken(String authorizationCode, String clientSecret) {
        try {
            RestClient restClient = RestClient.create();
            return restClient.post()
                    .uri(appleAuthUrl + "/auth/token")
                    .headers(headers -> headers.add("Content-Type", "application/x-www-form-urlencoded"))
                    .body(createTokenRequestParams(authorizationCode, clientSecret))
                    .retrieve()
                    .body(AppleTokenResponse.class);
        } catch (Exception e) {
            throw new CustomAppleLoginException(TOKEN_REQUEST_FAILED, "failed to get token from Apple server");
        }
    }

    private MultiValueMap<String, String> createTokenRequestParams(String authorizationCode, String clientSecret) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", appleClientId);
        params.add("client_secret", clientSecret);
        params.add("code", authorizationCode);
        params.add("redirect_uri", appleRedirectUrl);
        return params;
    }

    private AuthResponse authenticate(String socialId, String email, String nickname, String appleRefreshToken) {
        User user = userRepository.findBySocialId(socialId);
        if (user == null) {
            user = userRepository.save(User.createBySocial(socialId, nickname, email));
            userAppleTokenRepository.save(UserAppleToken.create(user, appleRefreshToken));
        }

        UserAuthentication userAuthentication = new UserAuthentication(user.getUserId(), null, null);
        String accessToken = jwtProvider.generateAccessToken(userAuthentication);
        String refreshToken = jwtProvider.generateRefreshToken(userAuthentication);

        refreshTokenRepository.save(new RefreshToken(refreshToken, user.getUserId()));

        boolean isRegister = !user.getNickname().contains("*");

        return AuthResponse.of(accessToken, refreshToken, isRegister);
    }

    private MultiValueMap<String, String> createUserRevokeParams(String clientSecret, String appleRefreshToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", appleClientId);
        params.add("client_secret", clientSecret);
        params.add("token", appleRefreshToken);
        params.add("token_type_hint", "refresh_token");
        return params;
    }
}
