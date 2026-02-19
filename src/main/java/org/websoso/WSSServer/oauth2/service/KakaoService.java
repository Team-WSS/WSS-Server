package org.websoso.WSSServer.oauth2.service;

import static org.websoso.WSSServer.exception.error.CustomKakaoError.INVALID_KAKAO_ACCESS_TOKEN;
import static org.websoso.WSSServer.exception.error.CustomKakaoError.KAKAO_SERVER_ERROR;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.websoso.WSSServer.config.jwt.CustomAuthenticationToken;
import org.websoso.WSSServer.config.jwt.JwtProvider;
import org.websoso.WSSServer.oauth2.domain.RefreshToken;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.dto.auth.AuthResponse;
import org.websoso.WSSServer.exception.exception.CustomKakaoException;
import org.websoso.WSSServer.oauth2.dto.KakaoUserInfo;
import org.websoso.WSSServer.oauth2.repository.RefreshTokenRepository;
import org.websoso.WSSServer.user.repository.UserRepository;
import org.websoso.WSSServer.notification.service.DiscordMessageClient;

@Service
@Transactional
@RequiredArgsConstructor
public class KakaoService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final DiscordMessageClient discordMessageClient;

    @Value("${kakao.user-info-url}")
    private String kakaoUserInfoUrl;

    @Value("${kakao.logout-url}")
    private String kakaoLogoutUrl;

    @Value("${kakao.admin-key}")
    private String kakaoAdminKey;

    @Value("${kakao.unlink-url}")
    private String kakaoUnlinkUrl;

    public KakaoUserInfo getUserInfo(String accessToken) {
        return RestClient.create()
                .get()
                .uri(kakaoUserInfoUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new CustomKakaoException(INVALID_KAKAO_ACCESS_TOKEN, "invalid kakao access token");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new CustomKakaoException(KAKAO_SERVER_ERROR, "kakao server error");
                })
                .body(KakaoUserInfo.class);
    }

    public void kakaoLogout(User user) {
        String socialId = user.getSocialId();
        String kakaoUserInfoId = socialId.replaceFirst("kakao_", "");

        MultiValueMap<String, String> logoutInfoBodies = new LinkedMultiValueMap<>();
        logoutInfoBodies.add("target_id_type", "user_id");
        logoutInfoBodies.add("target_id", kakaoUserInfoId);

        RestClient restClient = RestClient.create();
        restClient.post()
                .uri(kakaoLogoutUrl)
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoAdminKey)
                .body(logoutInfoBodies)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new CustomKakaoException(KAKAO_SERVER_ERROR,
                            "Kakao server error during logout");
                })
                .toBodilessEntity();
    }

    public void unlinkFromKakao(User user) {
        String socialId = user.getSocialId();
        String kakaoUserInfoId = socialId.replaceFirst("kakao_", "");

        MultiValueMap<String, String> withdrawInfoBodies = new LinkedMultiValueMap<>();
        withdrawInfoBodies.add("target_id_type", "user_id");
        withdrawInfoBodies.add("target_id", kakaoUserInfoId);

        RestClient.create()
                .post()
                .uri(kakaoUnlinkUrl)
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoAdminKey)
                .body(withdrawInfoBodies)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new CustomKakaoException(KAKAO_SERVER_ERROR,
                            "Kakao server error during logout");
                })
                .toBodilessEntity();
    }
}
