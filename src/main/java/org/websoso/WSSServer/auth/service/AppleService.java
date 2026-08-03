package org.websoso.WSSServer.auth.service;

import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.USER_APPLE_REFRESH_TOKEN_NOT_FOUND;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.auth.client.AppleClient;
import org.websoso.WSSServer.auth.client.AppleIdTokenVerifier;
import org.websoso.WSSServer.auth.client.AppleKeyGenerator;
import org.websoso.WSSServer.auth.client.dto.AppleTokenResponse;
import org.websoso.WSSServer.auth.controller.dto.AppleIdUpdateRequest;
import org.websoso.WSSServer.auth.domain.UserAppleToken;
import org.websoso.WSSServer.auth.repository.UserAppleTokenRepository;
import org.websoso.WSSServer.auth.service.dto.AppleAuthResult;
import org.websoso.WSSServer.exception.exception.CustomAppleLoginException;
import org.websoso.WSSServer.user.domain.User;

@Service
@RequiredArgsConstructor
public class AppleService {

    private static final String APPLE_PREFIX = "apple";
    private static final String CLAIM_SUB = "sub";
    private static final String CLAIM_EMAIL = "email";

    private final UserAppleTokenRepository userAppleTokenRepository;
    private final AppleClient appleClient;
    private final AppleKeyGenerator appleKeyGenerator;
    private final AppleIdTokenVerifier appleIdTokenVerifier;

    /**
     * Apple ID Token을 검증하고 Authorization Code를 Apple Refresh Token으로 교환한다.
     *
     * @param authorizationCode Apple Authorization Code
     * @param appleIdToken      Apple ID Token
     * @return Apple 인증 결과
     */
    public AppleAuthResult authenticate(String authorizationCode, String appleIdToken) {
        Claims claims = appleIdTokenVerifier.verify(appleIdToken);

        String clientSecret = appleKeyGenerator.createClientSecret();
        AppleTokenResponse appleTokenResponse = appleClient.requestAppleToken(authorizationCode, clientSecret);

        return AppleAuthResult.of(
                claims.get(CLAIM_SUB, String.class),
                claims.get(CLAIM_EMAIL, String.class),
                appleTokenResponse.getRefreshToken()
        );
    }

    @Transactional
    public void upsertRefreshToken(User user, String appleRefreshToken) {
        userAppleTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        token -> token.updateRefreshToken(appleRefreshToken),
                        () -> userAppleTokenRepository.save(UserAppleToken.create(user, appleRefreshToken))
                );
    }

    @Transactional
    public void unlinkFromApple(User user) {
        UserAppleToken userAppleToken = userAppleTokenRepository.findByUser(user).orElseThrow(
                () -> new CustomAppleLoginException(USER_APPLE_REFRESH_TOKEN_NOT_FOUND,
                        "cannot find the user Apple refresh token"));

        appleClient.revokeAppleToken(appleKeyGenerator.createClientSecret(), userAppleToken.getAppleRefreshToken());

        userAppleTokenRepository.delete(userAppleToken);
    }

    /**
     * 애플 로그인시, 기존 SocialId와 Refresh Token을 업데이트함
     *
     * @param user    User
     * @param request AppleIdUpdateRequest
     */
    @Transactional
    public void syncSocialId(User user, AppleIdUpdateRequest request) {

        UserAppleToken userAppleToken = userAppleTokenRepository.findByUser(user)
                .orElse(null);

        if (userAppleToken == null) {
            return;
        }

        AppleAuthResult appleAuthResult = authenticate(request.authorizationCode(), request.idToken());

        String customSocialId = APPLE_PREFIX + "_" + appleAuthResult.userIdentifier();

        user.syncSocialId(customSocialId);
        userAppleToken.syncRefreshToken(appleAuthResult.appleRefreshToken());
    }
}
