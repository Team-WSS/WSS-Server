package org.websoso.WSSServer.apple.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.websoso.WSSServer.dto.user.LoginResponse;
import org.websoso.WSSServer.service.UserService;

@Service
@RequiredArgsConstructor
public class AppleService {

    private static final String GRANT_TYPE = "authorization_code";
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final long TOKEN_EXPIRATION_TIME = 179L * 24 * 60 * 60 * 1000; // 약 6개월 미만

    @Value("${apple.auth.team-id}")
    private String appleTeamId;

    @Value("${apple.auth.key.id}")
    private String appleLoginKey;

    @Value("${apple.auth.client-id}")
    private String appleClientId;

    @Value("${apple.auth.redirect-url}")
    private String appleRedirectUrl;

    @Value("${apple.auth.key.path}")
    private String appleKeyPath;

    @Value("${apple.auth.iss}")
    private String appleAuthUrl;

    private final UserService userService;

    public LoginResponse getAppleInfo(String authorizationCode) throws Exception {
        validateAuthorizationCode(authorizationCode);
        String clientSecret = createClientSecret();

        try {
            JSONObject tokenResponse = requestToken(authorizationCode, clientSecret);
            JSONObject payload = parseIdToken((String) tokenResponse.get("id_token"));

            String socialId = (String) payload.get("sub");
            String email = (String) payload.get("email");

            return userService.signUpOrSignInWithApple(socialId, email);
        } catch (Exception e) {
            throw new Exception("Failed to retrieve user information from Apple", e);
        }
    }

    private void validateAuthorizationCode(String code) throws Exception {
        if (code == null || code.isBlank()) {
            throw new Exception("Authorization code is missing");
        }
    }

    private JSONObject requestToken(String authorizationCode, String clientSecret) throws Exception {
        HttpHeaders headers = createHttpHeaders();
        MultiValueMap<String, String> params = createTokenRequestParams(authorizationCode, clientSecret);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                appleAuthUrl + "/auth/token",
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        JSONParser jsonParser = new JSONParser();
        return (JSONObject) jsonParser.parse(response.getBody());
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", CONTENT_TYPE);
        return headers;
    }

    private MultiValueMap<String, String> createTokenRequestParams(String authorizationCode, String clientSecret) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", GRANT_TYPE);
        params.add("client_id", appleClientId);
        params.add("client_secret", clientSecret);
        params.add("code", authorizationCode);
        params.add("redirect_uri", appleRedirectUrl);
        return params;
    }

    private JSONObject parseIdToken(String idToken) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(idToken);
        ReadOnlyJWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(claimsSet.toJSONObject().toJSONString(), JSONObject.class);
    }

    private String createClientSecret() throws Exception {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(appleLoginKey).build();
        JWTClaimsSet claimsSet = buildJwtClaimsSet();

        SignedJWT jwt = new SignedJWT(header, claimsSet);
        signJwt(jwt);

        return jwt.serialize();
    }

    private JWTClaimsSet buildJwtClaimsSet() {
        Date now = new Date();
        JWTClaimsSet claimsSet = new JWTClaimsSet();

        claimsSet.setIssuer(appleTeamId);
        claimsSet.setIssueTime(now);
        claimsSet.setExpirationTime(new Date(now.getTime() + TOKEN_EXPIRATION_TIME));
        claimsSet.setAudience(appleAuthUrl);
        claimsSet.setSubject(appleClientId);

        return claimsSet;
    }

    private void signJwt(SignedJWT jwt) throws Exception {
        try {
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(readPrivateKey(appleKeyPath));
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            ECPrivateKey ecPrivateKey = (ECPrivateKey) keyFactory.generatePrivate(spec);
            JWSSigner signer = new ECDSASigner(ecPrivateKey.getS());
            jwt.sign(signer);
        } catch (JOSEException e) {
            throw new Exception("Failed to create client secret", e);
        }
    }

    private byte[] readPrivateKey(String keyPath) throws Exception {
        Resource resource = new ClassPathResource(keyPath);
        try (PemReader pemReader = new PemReader(new FileReader(resource.getFile()))) {
            PemObject pemObject = pemReader.readPemObject();
            return pemObject.getContent();
        } catch (IOException e) {
            throw new Exception("Failed to read private key", e);
        }
    }
}
