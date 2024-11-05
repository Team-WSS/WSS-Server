package org.websoso.WSSServer.oauth2.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.config.jwt.JWTUtil;
import org.websoso.WSSServer.dto.auth.AppleLoginRequest;
import org.websoso.WSSServer.dto.auth.AuthResponse;
import org.websoso.WSSServer.repository.RefreshTokenRepository;
import org.websoso.WSSServer.service.RedisService;
import org.websoso.WSSServer.service.UserService;

@Service
@RequiredArgsConstructor
public class AppleService {

    private static final String APPLE_PREFIX = "apple";
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisService redisService;
    private final JWTUtil jwtUtil;

    public AuthResponse getUserInfoFromApple(AppleLoginRequest request) {
        String customSocialId = APPLE_PREFIX + "_" + request.userIdentifier();
        String defaultNickname = APPLE_PREFIX.charAt(0) + "*" + request.userIdentifier().substring(7, 15);

        return userService.authenticateWithApple(customSocialId, request.email(), defaultNickname);
    }

    public void logout(HttpServletRequest request, String refreshToken) {
        String accessToken = request.getHeader("Authorization").substring(7);

        refreshTokenRepository.findByRefreshToken(refreshToken).ifPresent(refreshTokenRepository::delete);

        redisService.setBlackList(accessToken, "logout",
                jwtUtil.getExpiration(accessToken).getTime() - System.currentTimeMillis());
    }
}
