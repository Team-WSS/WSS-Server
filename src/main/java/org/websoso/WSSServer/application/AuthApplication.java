package org.websoso.WSSServer.application;

import static org.websoso.WSSServer.exception.error.CustomAuthError.INVALID_TOKEN;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.config.jwt.CustomAuthenticationToken;
import org.websoso.WSSServer.config.jwt.JWTUtil;
import org.websoso.WSSServer.config.jwt.JwtProvider;
import org.websoso.WSSServer.config.jwt.JwtValidationType;
import org.websoso.WSSServer.dto.auth.AuthResponse;
import org.websoso.WSSServer.dto.auth.LogoutRequest;
import org.websoso.WSSServer.dto.auth.ReissueResponse;
import org.websoso.WSSServer.dto.user.LoginResponse;
import org.websoso.WSSServer.exception.exception.CustomAuthException;
import org.websoso.WSSServer.oauth2.dto.KakaoUserInfo;
import org.websoso.WSSServer.oauth2.service.KakaoService;
import org.websoso.WSSServer.oauth2.repository.RefreshTokenRepository;
import org.websoso.WSSServer.oauth2.domain.RefreshToken;
import org.websoso.WSSServer.oauth2.service.TokenService;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.notification.repository.UserDeviceRepository;
import org.websoso.WSSServer.user.service.UserService;

@Service
@RequiredArgsConstructor
public class AuthApplication {

    private final TokenService tokenService;
    private final JwtProvider jwtProvider;
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final UserService userService;
    private final KakaoService kakaoService;
    private static final String KAKAO_PREFIX = "kakao";
    private static final String APPLE_PREFIX = "apple";

    @Transactional
    public ReissueResponse reissue(String refreshToken) {
        // 1. 토큰 유효성 검증
        if (jwtUtil.validateJWT(refreshToken) != JwtValidationType.VALID_REFRESH) {
            throw new CustomAuthException(INVALID_TOKEN, "given token is invalid token for reissue");
        }

        // 2. 저장된 토큰 조회
        RefreshToken storedRefreshToken = tokenService.findRefreshTokenOrThrow(refreshToken);

        // 3. 새로운 토큰 생성
        Long userId = jwtUtil.getUserIdFromJwt(refreshToken);
        CustomAuthenticationToken customAuthenticationToken = new CustomAuthenticationToken(userId, null, null);

        String newAccessToken = jwtProvider.generateAccessToken(customAuthenticationToken);
        String newRefreshToken = jwtProvider.generateRefreshToken(customAuthenticationToken);

        // 4. 리프레시 토큰 교체
        tokenService.rotateRefreshToken(storedRefreshToken, newRefreshToken, userId);

        return ReissueResponse.of(newAccessToken, newRefreshToken);
    }

    @Transactional
    public AuthResponse loginKakao(String kakaoAccessToken) {
        // 1. 카카오 로그인 인증
        KakaoUserInfo kakaoUserInfo = kakaoService.getUserInfo(kakaoAccessToken);

        // 2. 사용자 정보 불러오기 / 생성
        User user = userService.getOrCreateKakaoUser(kakaoUserInfo);

        // 3. Access / Refresh Token 생성
        CustomAuthenticationToken customAuthenticationToken = CustomAuthenticationToken.create(user.getUserId());
        String accessToken = jwtProvider.generateAccessToken(customAuthenticationToken);
        String refreshToken = jwtProvider.generateRefreshToken(customAuthenticationToken);

        // 4. Refresh Token 저장
        tokenService.saveRefreshToken(user, refreshToken);

        boolean isRegister = !user.isTemporaryNickname();
        return AuthResponse.of(accessToken, refreshToken, isRegister);
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
