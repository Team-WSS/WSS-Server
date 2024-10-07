package org.websoso.WSSServer.oauth2.service;

import static org.websoso.WSSServer.exception.error.CustomKakaoError.INVALID_KAKAO_ACCESS_TOKEN;
import static org.websoso.WSSServer.exception.error.CustomKakaoError.KAKAO_SERVER_ERROR;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.websoso.WSSServer.config.jwt.JwtProvider;
import org.websoso.WSSServer.config.jwt.UserAuthentication;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.auth.AuthResponse;
import org.websoso.WSSServer.exception.exception.CustomKakaoException;
import org.websoso.WSSServer.oauth2.dto.KakaoUserInfo;
import org.websoso.WSSServer.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class KakaoService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Value("${kakao.user-info-url}")
    private String kakaoUserInfoUrl;

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

        User user = userRepository.findBySocialId(socialId);
        if (user == null) {
            user = userRepository.save(User.createBySocial(socialId, defaultNickname, kakaoUserInfo.email()));
        }

        UserAuthentication userAuthentication = new UserAuthentication(user.getUserId(), null, null);
        String accessToken = jwtProvider.generateAccessToken(userAuthentication);
        String refreshToken = jwtProvider.generateRefreshToken(userAuthentication);

    }
}
