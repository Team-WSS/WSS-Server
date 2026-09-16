package org.websoso.WSSServer.auth.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.CLIENT_SECRET_CREATION_FAILED;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;
import org.websoso.WSSServer.exception.exception.CustomAppleLoginException;

class AppleKeyGeneratorTest {

    private static final String APPLE_LOGIN_KEY = "test-key-id";
    private static final String APPLE_TEAM_ID = "test-team-id";
    private static final String APPLE_CLIENT_ID = "org.websoso.app";
    private static final String APPLE_AUTH_URL = "https://appleid.apple.com";
    private static final long EXPIRATION_TIME = 3600000L;

    @TempDir
    Path tempDir;

    private AppleKeyGenerator appleKeyGenerator;

    @BeforeEach
    void setUp() {
        appleKeyGenerator = new AppleKeyGenerator(new DefaultResourceLoader());
        ReflectionTestUtils.setField(appleKeyGenerator, "appleLoginKey", APPLE_LOGIN_KEY);
        ReflectionTestUtils.setField(appleKeyGenerator, "tokenExpirationTime", EXPIRATION_TIME);
        ReflectionTestUtils.setField(appleKeyGenerator, "appleTeamId", APPLE_TEAM_ID);
        ReflectionTestUtils.setField(appleKeyGenerator, "appleClientId", APPLE_CLIENT_ID);
        ReflectionTestUtils.setField(appleKeyGenerator, "appleAuthUrl", APPLE_AUTH_URL);
    }

    @DisplayName("프라이빗 키 파일을 읽을 수 없으면 클라이언트 시크릿 생성 실패 예외로 변환된다")
    @Test
    void createClientSecret_privateKeyReadFailed() {
        ReflectionTestUtils.setField(appleKeyGenerator, "appleKeyPath",
                tempDir.resolve("not-exist.p8").toString());

        assertThatThrownBy(() -> appleKeyGenerator.createClientSecret())
                .isInstanceOf(CustomAppleLoginException.class)
                .extracting(throwable -> ((CustomAppleLoginException) throwable).getICustomError())
                .isEqualTo(CLIENT_SECRET_CREATION_FAILED);
    }

    @DisplayName("프라이빗 키 형식이 잘못되면 클라이언트 시크릿 생성 실패 예외로 변환된다")
    @Test
    void createClientSecret_invalidPrivateKeyFormat() throws IOException {
        Path invalidKey = tempDir.resolve("invalid.p8");
        Files.writeString(invalidKey, """
                -----BEGIN PRIVATE KEY-----
                bm90LWEtdmFsaWQtcHJpdmF0ZS1rZXk=
                -----END PRIVATE KEY-----
                """);
        ReflectionTestUtils.setField(appleKeyGenerator, "appleKeyPath", invalidKey.toString());

        assertThatThrownBy(() -> appleKeyGenerator.createClientSecret())
                .isInstanceOf(CustomAppleLoginException.class)
                .extracting(throwable -> ((CustomAppleLoginException) throwable).getICustomError())
                .isEqualTo(CLIENT_SECRET_CREATION_FAILED);
    }

    @DisplayName("유효한 프라이빗 키로 서명된 클라이언트 시크릿을 생성한다")
    @Test
    void createClientSecret_success() throws IOException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        Path validKey = tempDir.resolve("valid.p8");
        Files.writeString(validKey, toPrivateKeyPem(generateEcKeyPair()));
        ReflectionTestUtils.setField(appleKeyGenerator, "appleKeyPath", validKey.toString());

        String clientSecret = appleKeyGenerator.createClientSecret();

        assertThat(clientSecret.split("\\.")).hasSize(3);
    }

    private KeyPair generateEcKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"));
        return keyPairGenerator.generateKeyPair();
    }

    private String toPrivateKeyPem(KeyPair keyPair) {
        String encoded = Base64.getMimeEncoder(64, System.lineSeparator().getBytes())
                .encodeToString(keyPair.getPrivate().getEncoded());
        return "-----BEGIN PRIVATE KEY-----" + System.lineSeparator()
                + encoded + System.lineSeparator()
                + "-----END PRIVATE KEY-----" + System.lineSeparator();
    }
}
