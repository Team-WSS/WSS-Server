package org.websoso.WSSServer.oauth2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.dto.auth.AppleLoginRequest;
import org.websoso.WSSServer.dto.auth.AuthResponse;
import org.websoso.WSSServer.repository.RefreshTokenRepository;
import org.websoso.WSSServer.service.UserService;

@Service
@RequiredArgsConstructor
public class AppleService {

    private static final String APPLE_PREFIX = "apple";
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthResponse getUserInfoFromApple(AppleLoginRequest request) {
        String customSocialId = APPLE_PREFIX + "_" + request.userIdentifier();
        String defaultNickname = APPLE_PREFIX.charAt(0) + "*" + request.userIdentifier().substring(7, 15);

        return userService.authenticateWithApple(customSocialId, request.email(), defaultNickname);
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByRefreshToken(refreshToken).ifPresent(refreshTokenRepository::delete);
    }
}
