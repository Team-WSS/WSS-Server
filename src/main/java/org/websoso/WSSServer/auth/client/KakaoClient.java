package org.websoso.WSSServer.auth.client;

import static org.websoso.WSSServer.exception.error.CustomKakaoError.INVALID_KAKAO_ACCESS_TOKEN;
import static org.websoso.WSSServer.exception.error.CustomKakaoError.KAKAO_SERVER_ERROR;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.websoso.WSSServer.auth.client.dto.KakaoUserInfo;
import org.websoso.WSSServer.exception.exception.CustomKakaoException;

@Component
@RequiredArgsConstructor
public class KakaoClient {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ADMIN_KEY_PREFIX = "KakaoAK ";
    private static final String TARGET_ID_TYPE = "target_id_type";
    private static final String TARGET_ID_TYPE_USER_ID = "user_id";
    private static final String TARGET_ID = "target_id";

    private final RestClient kakaoRestClient;

    @Value("${kakao.user-info-url}")
    private String kakaoUserInfoUrl;

    @Value("${kakao.logout-url}")
    private String kakaoLogoutUrl;

    @Value("${kakao.admin-key}")
    private String kakaoAdminKey;

    @Value("${kakao.unlink-url}")
    private String kakaoUnlinkUrl;

    public KakaoUserInfo getUserInfo(String accessToken) {
        return kakaoRestClient
                .get()
                .uri(kakaoUserInfoUrl)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new CustomKakaoException(INVALID_KAKAO_ACCESS_TOKEN, "invalid kakao access token");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new CustomKakaoException(KAKAO_SERVER_ERROR, "kakao server error");
                })
                .body(KakaoUserInfo.class);
    }

    public void logout(String providerUserId) {
        requestAdminApi(kakaoLogoutUrl, providerUserId, "logout");
    }

    public void unlink(String providerUserId) {
        requestAdminApi(kakaoUnlinkUrl, providerUserId, "unlink");
    }

    private void requestAdminApi(String url, String providerUserId, String operation) {
        kakaoRestClient
                .post()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header(HttpHeaders.AUTHORIZATION, ADMIN_KEY_PREFIX + kakaoAdminKey)
                .body(createTargetIdParams(providerUserId))
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new CustomKakaoException(KAKAO_SERVER_ERROR,
                            "kakao server error during " + operation);
                })
                .toBodilessEntity();
    }

    private MultiValueMap<String, String> createTargetIdParams(String providerUserId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(TARGET_ID_TYPE, TARGET_ID_TYPE_USER_ID);
        params.add(TARGET_ID, providerUserId);
        return params;
    }
}
