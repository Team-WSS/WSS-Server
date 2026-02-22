package org.websoso.WSSServer.oauth2.service;

import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.TOKEN_REQUEST_FAILED;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.websoso.WSSServer.dto.auth.ApplePublicKeys;
import org.websoso.WSSServer.dto.auth.AppleTokenResponse;
import org.websoso.WSSServer.exception.exception.CustomAppleLoginException;

@Component
@RequiredArgsConstructor
public class AppleClient {

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
        return RestClient.create()
                .get()
                .uri(applePublicKeysUrl)
                .retrieve()
                .body(ApplePublicKeys.class);
    }

    // Authorization Code로 토큰 발급 요청
    public AppleTokenResponse requestAppleToken(String authorizationCode, String clientSecret) {
        try {
            return RestClient.create()
                    .post()
                    .uri(appleAuthUrl + "/auth/token")
                    .headers(headers -> headers.add("Content-Type", "application/x-www-form-urlencoded"))
                    .body(createTokenRequestParams(authorizationCode, clientSecret))
                    .retrieve()
                    .body(AppleTokenResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
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
}
