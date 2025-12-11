package org.websoso.WSSServer.application;

import static org.websoso.WSSServer.exception.error.CustomAuthError.INVALID_TOKEN;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.config.jwt.CustomAuthenticationToken;
import org.websoso.WSSServer.config.jwt.JWTUtil;
import org.websoso.WSSServer.config.jwt.JwtProvider;
import org.websoso.WSSServer.config.jwt.JwtValidationType;
import org.websoso.WSSServer.dto.auth.LogoutRequest;
import org.websoso.WSSServer.dto.auth.ReissueResponse;
import org.websoso.WSSServer.dto.user.LoginResponse;
import org.websoso.WSSServer.exception.exception.CustomAuthException;
import org.websoso.WSSServer.oauth2.service.KakaoService;
import org.websoso.WSSServer.repository.RefreshTokenRepository;
import org.websoso.WSSServer.user.domain.RefreshToken;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.repository.UserDeviceRepository;
import org.websoso.WSSServer.user.service.UserService;

@Service
@RequiredArgsConstructor
public class AuthApplication {
    private final JwtProvider jwtProvider;
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final UserService userService;
    private final KakaoService kakaoService;
    private static final String KAKAO_PREFIX = "kakao";
    private static final String APPLE_PREFIX = "apple";

    public ReissueResponse reissue(String refreshToken) {
        RefreshToken storedRefreshToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new CustomAuthException(INVALID_TOKEN, "given token is invalid token for reissue"));

        if (jwtUtil.validateJWT(refreshToken) != JwtValidationType.VALID_REFRESH) {
            throw new CustomAuthException(INVALID_TOKEN, "given token is invalid token for reissue");
        }

        Long userId = jwtUtil.getUserIdFromJwt(refreshToken);
        CustomAuthenticationToken customAuthenticationToken = new CustomAuthenticationToken(userId, null, null);
        String newAccessToken = jwtProvider.generateAccessToken(customAuthenticationToken);
        String newRefreshToken = jwtProvider.generateRefreshToken(customAuthenticationToken);

        refreshTokenRepository.delete(storedRefreshToken);
        refreshTokenRepository.save(new RefreshToken(newRefreshToken, userId));

        return ReissueResponse.of(newAccessToken, newRefreshToken);
    }

    // TODO: getUserOrException -> existUserOrException 변경
    @Transactional(readOnly = true)
    public LoginResponse login(Long userId) {
        User user = userService.getUserOrException(userId);

        CustomAuthenticationToken customAuthenticationToken = new CustomAuthenticationToken(user.getUserId(), null,
                null);
        String token = jwtProvider.generateAccessToken(customAuthenticationToken);

        return LoginResponse.of(token);
    }

    public void logout(User user, LogoutRequest request) {
        refreshTokenRepository.findByRefreshToken(request.refreshToken())
                .ifPresent(refreshTokenRepository::delete);

        userDeviceRepository.deleteByUserAndDeviceIdentifier(user, request.deviceIdentifier());

        if (user.getSocialId().startsWith(KAKAO_PREFIX)) {
            kakaoService.kakaoLogout(user);
        }
    }
}
