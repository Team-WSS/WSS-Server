package org.websoso.WSSServer.auth.service;

import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.USER_APPLE_REFRESH_TOKEN_NOT_FOUND;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.websoso.WSSServer.auth.client.AppleClient;
import org.websoso.WSSServer.auth.client.AppleIdTokenVerifier;
import org.websoso.WSSServer.auth.client.AppleKeyGenerator;
import org.websoso.WSSServer.auth.client.dto.AppleTokenResponse;
import org.websoso.WSSServer.auth.controller.dto.AppleIdUpdateRequest;
import org.websoso.WSSServer.auth.domain.UserAppleToken;
import org.websoso.WSSServer.auth.repository.UserAppleTokenRepository;
import org.websoso.WSSServer.exception.exception.CustomAppleLoginException;
import org.websoso.WSSServer.user.domain.User;

@Transactional
@Service
@RequiredArgsConstructor
public class AppleService {

    private static final String APPLE_PREFIX = "apple";
    private static final String CLAIM_SUB = "sub";

    private final UserAppleTokenRepository userAppleTokenRepository;
    private final AppleClient appleClient;
    private final AppleKeyGenerator appleKeyGenerator;
    private final AppleIdTokenVerifier appleIdTokenVerifier;

    @Value("${apple.client-id}")
    private String appleClientId;

    @Value("${apple.iss}")
    private String appleAuthUrl;

    public void upsertRefreshToken(User user, String appleRefreshToken) {
        userAppleTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        token -> token.updateRefreshToken(appleRefreshToken),
                        () -> userAppleTokenRepository.save(UserAppleToken.create(user, appleRefreshToken))
                );
    }

    public void unlinkFromApple(User user) {
        UserAppleToken userAppleToken = userAppleTokenRepository.findByUser(user).orElseThrow(
                () -> new CustomAppleLoginException(USER_APPLE_REFRESH_TOKEN_NOT_FOUND,
                        "cannot find the user Apple refresh token"));

        RestClient restClient = RestClient.create();
        restClient.post()
                .uri(appleAuthUrl + "/auth/revoke")
                .headers(headers -> headers.add("Content-Type", "application/x-www-form-urlencoded"))
                .body(createUserRevokeParams(appleKeyGenerator.createClientSecret(), userAppleToken.getAppleRefreshToken()))
                .retrieve()
                .body(String.class);

        userAppleTokenRepository.delete(userAppleToken);
    }

    /**
     * 애플 로그인시, 기존 SocialId와 Refresh Token을 업데이트함
     *
     * @param user    User
     * @param request AppleIdUpdateRequest
     */
    public void syncSocialId(User user, AppleIdUpdateRequest request) {

        UserAppleToken userAppleToken = userAppleTokenRepository.findByUser(user)
                .orElse(null);

        if (userAppleToken == null) {
            return;
        }

        String appleToken = request.idToken();
        Claims claims = appleIdTokenVerifier.verify(appleToken);

        AppleTokenResponse appleTokenResponse = appleClient.requestAppleToken(request.authorizationCode(),
                appleKeyGenerator.createClientSecret());

        String userIdentifier = claims.get(CLAIM_SUB, String.class);
        String customSocialId = APPLE_PREFIX + "_" + userIdentifier;

        user.syncSocialId(customSocialId);
        userAppleToken.syncRefreshToken(appleTokenResponse.getRefreshToken());
    }

    private MultiValueMap<String, String> createUserRevokeParams(String clientSecret, String appleRefreshToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", appleClientId);
        params.add("client_secret", clientSecret);
        params.add("token", appleRefreshToken);
        params.add("token_type_hint", "refresh_token");
        return params;
    }
}
