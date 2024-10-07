package org.websoso.WSSServer.oauth2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.websoso.WSSServer.dto.auth.AuthResponse;
import org.websoso.WSSServer.oauth2.dto.KakaoUserInfo;

@Service
@RequiredArgsConstructor
public class KakaoService {

    @Value("${kakao.user-info-url}")
    private String kakaoUserInfoUrl;

    public AuthResponse getUserInfoFromKakao(String kakaoAccessToken) {
        RestClient restClient = RestClient.create();

        KakaoUserInfo kakaoUserInfo = restClient.get()
                .uri(kakaoUserInfoUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
                .retrieve()
                .body(KakaoUserInfo.class);

    }
}
