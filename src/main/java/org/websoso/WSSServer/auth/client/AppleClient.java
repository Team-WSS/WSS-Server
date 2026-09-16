package org.websoso.WSSServer.auth.client;

import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.PUBLIC_KEY_REQUEST_FAILED;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.TOKEN_REQUEST_FAILED;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.TOKEN_REVOKE_FAILED;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.websoso.WSSServer.auth.client.dto.ApplePublicKeys;
import org.websoso.WSSServer.auth.client.dto.AppleTokenResponse;
import org.websoso.WSSServer.exception.error.CustomAppleLoginError;
import org.websoso.WSSServer.exception.exception.CustomAppleLoginException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleClient {

    private static final String TOKEN_PATH = "/auth/token";
    private static final String REVOKE_PATH = "/auth/revoke";
    private static final String GRANT_TYPE = "grant_type";
    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String CODE = "code";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String TOKEN = "token";
    private static final String TOKEN_TYPE_HINT = "token_type_hint";

    private static final String PUBLIC_KEY_REQUEST = "public key request";
    private static final String TOKEN_REQUEST = "token request";
    private static final String TOKEN_REVOKE = "token revoke";

    private final RestClient appleRestClient;

    @Value("${apple.public-keys-url}")
    private String applePublicKeysUrl;

    @Value("${apple.client-id}")
    private String appleClientId;

    @Value("${apple.redirect-url}")
    private String appleRedirectUrl;

    @Value("${apple.iss}")
    private String appleAuthUrl;

    // 애플 공개키 목록 가져오기
    public ApplePublicKeys getApplePublicKeys() {
        try {
            return appleRestClient
                    .get()
                    .uri(applePublicKeysUrl)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) ->
                            handleErrorResponse(PUBLIC_KEY_REQUEST_FAILED, PUBLIC_KEY_REQUEST, response))
                    .body(ApplePublicKeys.class);
        } catch (RestClientException e) {
            throw convert(PUBLIC_KEY_REQUEST_FAILED, PUBLIC_KEY_REQUEST, e);
        }
    }

    // Authorization Code로 토큰 발급 요청
    public AppleTokenResponse requestAppleToken(String authorizationCode, String clientSecret) {
        try {
            return appleRestClient
                    .post()
                    .uri(appleAuthUrl + TOKEN_PATH)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(createTokenRequestParams(authorizationCode, clientSecret))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) ->
                            handleErrorResponse(TOKEN_REQUEST_FAILED, TOKEN_REQUEST, response))
                    .body(AppleTokenResponse.class);
        } catch (RestClientException e) {
            throw convert(TOKEN_REQUEST_FAILED, TOKEN_REQUEST, e);
        }
    }

    // Apple Refresh Token 폐기 요청
    public void revokeAppleToken(String clientSecret, String appleRefreshToken) {
        try {
            appleRestClient
                    .post()
                    .uri(appleAuthUrl + REVOKE_PATH)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(createRevokeRequestParams(clientSecret, appleRefreshToken))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) ->
                            handleErrorResponse(TOKEN_REVOKE_FAILED, TOKEN_REVOKE, response))
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw convert(TOKEN_REVOKE_FAILED, TOKEN_REVOKE, e);
        }
    }

    private MultiValueMap<String, String> createTokenRequestParams(String authorizationCode, String clientSecret) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);
        params.add(CLIENT_ID, appleClientId);
        params.add(CLIENT_SECRET, clientSecret);
        params.add(CODE, authorizationCode);
        params.add(REDIRECT_URI, appleRedirectUrl);
        return params;
    }

    private MultiValueMap<String, String> createRevokeRequestParams(String clientSecret, String appleRefreshToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(GRANT_TYPE, GRANT_TYPE_REFRESH_TOKEN);
        params.add(CLIENT_ID, appleClientId);
        params.add(CLIENT_SECRET, clientSecret);
        params.add(TOKEN, appleRefreshToken);
        params.add(TOKEN_TYPE_HINT, GRANT_TYPE_REFRESH_TOKEN);
        return params;
    }

    private void handleErrorResponse(CustomAppleLoginError error, String operation, ClientHttpResponse response)
            throws IOException {
        log.error("apple {} failed: status={}, body={}", operation, response.getStatusCode(), readBody(response));
        throw new CustomAppleLoginException(error, "apple " + operation + " failed");
    }

    private String readBody(ClientHttpResponse response) throws IOException {
        return new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private CustomAppleLoginException convert(CustomAppleLoginError error, String operation, RestClientException e) {
        log.error("apple {} failed: error={}", operation, e.getMessage(), e);
        return new CustomAppleLoginException(error, "apple " + operation + " failed");
    }
}
