package org.websoso.WSSServer.oauth2.service;

import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.CLIENT_SECRET_CREATION_FAILED;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.INVALID_APPLE_KEY;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.PRIVATE_KEY_READ_FAILED;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
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
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.dto.auth.ApplePublicKey;
import org.websoso.WSSServer.dto.auth.ApplePublicKeys;
import org.websoso.WSSServer.exception.exception.CustomAppleLoginException;

@Component
@RequiredArgsConstructor
public class AppleKeyGenerator {

    private static final int POSITIVE_SIGN_NUMBER = 1;

    @Value("${apple.key.id}")
    private String appleLoginKey;

    @Value("${apple.key.path}")
    private String appleKeyPath;

    @Value("${apple.expiration-time}")
    private long tokenExpirationTime;

    @Value("${apple.team-id}")
    private String appleTeamId;

    @Value("${apple.client-id}")
    private String appleClientId;

    @Value("${apple.iss}")
    private String appleAuthUrl;

    private final ResourceLoader resourceLoader;

    // 헤더의 kid, alg와 매칭되는 공개키를 찾아 RSA PublicKey 객체로 변환
    public PublicKey generatePublicKey(Map<String, String> headers, ApplePublicKeys publicKeys) {
        ApplePublicKey applePublicKey = publicKeys.getMatchingKey(
                headers.get("alg"),
                headers.get("kid")
        );
        return createPublicKey(applePublicKey);
    }

    private PublicKey createPublicKey(ApplePublicKey applePublicKey) {
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

    public String createClientSecret() {
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
        Resource resource = resourceLoader.getResource("file:" + keyPath);
        try (PemReader pemReader = new PemReader(new InputStreamReader(resource.getInputStream()))) {
            return pemReader.readPemObject().getContent();
        } catch (IOException e) {
            throw new CustomAppleLoginException(PRIVATE_KEY_READ_FAILED, "failed to read private key");
        }
    }

}
