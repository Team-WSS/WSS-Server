package org.websoso.WSSServer.oauth2.service;

import static org.websoso.WSSServer.domain.common.DiscordWebhookMessageType.JOIN;
import static org.websoso.WSSServer.domain.common.SocialLoginType.KAKAO;
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
import org.websoso.WSSServer.config.jwt.JwtProvider;
import org.websoso.WSSServer.config.jwt.UserAuthentication;
import org.websoso.WSSServer.domain.RefreshToken;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.DiscordWebhookMessage;
import org.websoso.WSSServer.dto.auth.AuthResponse;
import org.websoso.WSSServer.exception.exception.CustomKakaoException;
import org.websoso.WSSServer.oauth2.dto.KakaoUserInfo;
import org.websoso.WSSServer.repository.RefreshTokenRepository;
import org.websoso.WSSServer.repository.UserRepository;
import org.websoso.WSSServer.service.MessageFormatter;
import org.websoso.WSSServer.service.MessageService;

@Service
@Transactional
@RequiredArgsConstructor
public class KakaoService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final MessageService messageService;

    @Value("${kakao.user-info-url}")
    private String kakaoUserInfoUrl;

    @Value("${kakao.logout-url}")
    private String kakaoLogoutUrl;

    @Value("${kakao.admin-key}")
    private String kakaoAdminKey;

    @Value("${kakao.unlink-url}")
    private String kakaoUnlinkUrl;

    public AuthResponse getUserInfoFromKakao(String kakaoAccessToken) {
        RestClient restClient = RestClient.create();

        KakaoUserInfo kakaoUserInfo = restClient.get()
                .uri(kakaoUserInfoUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new CustomKakaoException(INVALID_KAKAO_ACCESS_TOKEN,
                            "given access token from kakao is invalid");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new CustomKakaoException(KAKAO_SERVER_ERROR,
                            "kakao server error");
                })
                .body(KakaoUserInfo.class);

        String socialId = "kakao_" + kakaoUserInfo.id();
        String defaultNickname = "k*" + kakaoUserInfo.id().toString().substring(2, 10);

        boolean isNewUser = false;
        User user = userRepository.findBySocialId(socialId);
        if (user == null) {
            user = userRepository.save(User.createBySocial(socialId, defaultNickname, kakaoUserInfo.email()));
            isNewUser = true;
        }

        UserAuthentication userAuthentication = new UserAuthentication(user.getUserId(), null, null);
        String accessToken = jwtProvider.generateAccessToken(userAuthentication);
        String refreshToken = jwtProvider.generateRefreshToken(userAuthentication);

        RefreshToken redisRefreshToken = new RefreshToken(refreshToken, user.getUserId());
        refreshTokenRepository.save(redisRefreshToken);

        boolean isRegister = !user.getNickname().contains("*");

        if (isNewUser) {
            messageService.sendDiscordWebhookMessage(
                    DiscordWebhookMessage.of(MessageFormatter.formatUserJoinMessage(user, KAKAO), JOIN));
        }
        return AuthResponse.of(accessToken, refreshToken, isRegister);
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
