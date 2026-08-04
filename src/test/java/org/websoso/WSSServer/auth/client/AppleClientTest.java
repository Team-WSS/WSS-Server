package org.websoso.WSSServer.auth.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.PUBLIC_KEY_REQUEST_FAILED;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.TOKEN_REQUEST_FAILED;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.TOKEN_REVOKE_FAILED;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.websoso.WSSServer.auth.client.dto.ApplePublicKeys;
import org.websoso.WSSServer.auth.client.dto.AppleTokenResponse;
import org.websoso.WSSServer.exception.exception.CustomAppleLoginException;

class AppleClientTest {

    private static final String PUBLIC_KEYS_URL = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_AUTH_URL = "https://appleid.apple.com";
    private static final String TOKEN_URL = APPLE_AUTH_URL + "/auth/token";
    private static final String REVOKE_URL = APPLE_AUTH_URL + "/auth/revoke";
    private static final String CLIENT_ID = "org.websoso.app";
    private static final String REDIRECT_URL = "https://websoso.org/apple/callback";
    private static final String AUTHORIZATION_CODE = "apple-authorization-code";
    private static final String CLIENT_SECRET = "apple-client-secret";
    private static final String APPLE_REFRESH_TOKEN = "apple-refresh-token";

    private MockRestServiceServer mockServer;
    private AppleClient appleClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();

        appleClient = new AppleClient(builder.build());
        ReflectionTestUtils.setField(appleClient, "applePublicKeysUrl", PUBLIC_KEYS_URL);
        ReflectionTestUtils.setField(appleClient, "appleAuthUrl", APPLE_AUTH_URL);
        ReflectionTestUtils.setField(appleClient, "appleClientId", CLIENT_ID);
        ReflectionTestUtils.setField(appleClient, "appleRedirectUrl", REDIRECT_URL);
    }

    @DisplayName("공개키 조회에 성공하면 애플 공개키 목록을 반환한다")
    @Test
    void getApplePublicKeys_success() {
        mockServer.expect(requestTo(PUBLIC_KEYS_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "keys": [
                            {"kty": "RSA", "kid": "test-kid", "alg": "RS256", "n": "test-n", "e": "AQAB"}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        ApplePublicKeys applePublicKeys = appleClient.getApplePublicKeys();

        assertThat(applePublicKeys.keys()).hasSize(1);
        assertThat(applePublicKeys.getMatchingKey("RS256", "test-kid").n()).isEqualTo("test-n");
        mockServer.verify();
    }

    @DisplayName("공개키 조회가 5xx로 실패하면 공개키 요청 실패 예외가 발생한다")
    @Test
    void getApplePublicKeys_serverError() {
        mockServer.expect(requestTo(PUBLIC_KEYS_URL))
                .andRespond(withServerError());

        assertThatThrownBy(() -> appleClient.getApplePublicKeys())
                .isInstanceOf(CustomAppleLoginException.class)
                .hasMessage("apple public key request failed")
                .extracting(throwable -> ((CustomAppleLoginException) throwable).getICustomError())
                .isEqualTo(PUBLIC_KEY_REQUEST_FAILED);
        mockServer.verify();
    }

    @DisplayName("토큰 교환에 성공하면 애플 토큰 응답을 반환한다")
    @Test
    void requestAppleToken_success() {
        mockServer.expect(requestTo(TOKEN_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string("grant_type=authorization_code"
                        + "&client_id=" + CLIENT_ID
                        + "&client_secret=" + CLIENT_SECRET
                        + "&code=" + AUTHORIZATION_CODE
                        + "&redirect_uri=https%3A%2F%2Fwebsoso.org%2Fapple%2Fcallback"))
                .andRespond(withSuccess("""
                        {
                          "access_token": "apple-access-token",
                          "expires_in": "3600",
                          "id_token": "apple-id-token",
                          "refresh_token": "apple-refresh-token",
                          "token_type": "bearer"
                        }
                        """, MediaType.APPLICATION_JSON));

        AppleTokenResponse response = appleClient.requestAppleToken(AUTHORIZATION_CODE, CLIENT_SECRET);

        assertThat(response.getRefreshToken()).isEqualTo(APPLE_REFRESH_TOKEN);
        assertThat(response.getIdToken()).isEqualTo("apple-id-token");
        mockServer.verify();
    }

    @DisplayName("토큰 교환이 4xx로 실패하면 토큰 요청 실패 예외가 발생한다")
    @Test
    void requestAppleToken_clientError() {
        mockServer.expect(requestTo(TOKEN_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": \"invalid_grant\"}"));

        assertThatThrownBy(() -> appleClient.requestAppleToken(AUTHORIZATION_CODE, CLIENT_SECRET))
                .isInstanceOf(CustomAppleLoginException.class)
                .hasMessage("apple token request failed")
                .extracting(throwable -> ((CustomAppleLoginException) throwable).getICustomError())
                .isEqualTo(TOKEN_REQUEST_FAILED);
        mockServer.verify();
    }

    @DisplayName("토큰 교환이 5xx로 실패하면 토큰 요청 실패 예외가 발생한다")
    @Test
    void requestAppleToken_serverError() {
        mockServer.expect(requestTo(TOKEN_URL))
                .andRespond(withServerError());

        assertThatThrownBy(() -> appleClient.requestAppleToken(AUTHORIZATION_CODE, CLIENT_SECRET))
                .isInstanceOf(CustomAppleLoginException.class)
                .extracting(throwable -> ((CustomAppleLoginException) throwable).getICustomError())
                .isEqualTo(TOKEN_REQUEST_FAILED);
        mockServer.verify();
    }

    @DisplayName("토큰 폐기 요청은 form body를 담아 전송한다")
    @Test
    void revokeAppleToken_success() {
        mockServer.expect(requestTo(REVOKE_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string("grant_type=refresh_token"
                        + "&client_id=" + CLIENT_ID
                        + "&client_secret=" + CLIENT_SECRET
                        + "&token=" + APPLE_REFRESH_TOKEN
                        + "&token_type_hint=refresh_token"))
                .andRespond(withSuccess());

        appleClient.revokeAppleToken(CLIENT_SECRET, APPLE_REFRESH_TOKEN);

        mockServer.verify();
    }

    @DisplayName("토큰 폐기가 4xx로 실패하면 토큰 폐기 실패 예외가 발생한다")
    @Test
    void revokeAppleToken_clientError() {
        mockServer.expect(requestTo(REVOKE_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> appleClient.revokeAppleToken(CLIENT_SECRET, APPLE_REFRESH_TOKEN))
                .isInstanceOf(CustomAppleLoginException.class)
                .hasMessage("apple token revoke failed")
                .extracting(throwable -> ((CustomAppleLoginException) throwable).getICustomError())
                .isEqualTo(TOKEN_REVOKE_FAILED);
        mockServer.verify();
    }

    @DisplayName("토큰 폐기가 5xx로 실패하면 토큰 폐기 실패 예외가 발생한다")
    @Test
    void revokeAppleToken_serverError() {
        mockServer.expect(requestTo(REVOKE_URL))
                .andRespond(withServerError());

        assertThatThrownBy(() -> appleClient.revokeAppleToken(CLIENT_SECRET, APPLE_REFRESH_TOKEN))
                .isInstanceOf(CustomAppleLoginException.class)
                .extracting(throwable -> ((CustomAppleLoginException) throwable).getICustomError())
                .isEqualTo(TOKEN_REVOKE_FAILED);
        mockServer.verify();
    }
}
