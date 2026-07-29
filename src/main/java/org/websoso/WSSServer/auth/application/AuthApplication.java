package org.websoso.WSSServer.auth.application;

import static org.websoso.WSSServer.exception.error.CustomAuthError.INVALID_TOKEN;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.auth.client.dto.AppleTokenResponse;
import org.websoso.WSSServer.auth.controller.dto.AuthResponse;
import org.websoso.WSSServer.auth.controller.dto.LogoutRequest;
import org.websoso.WSSServer.auth.controller.dto.ReissueResponse;
import org.websoso.WSSServer.auth.domain.RefreshToken;
import org.websoso.WSSServer.auth.jwt.CustomAuthenticationToken;
import org.websoso.WSSServer.auth.jwt.JWTUtil;
import org.websoso.WSSServer.auth.jwt.JwtProvider;
import org.websoso.WSSServer.auth.jwt.JwtValidationType;
import org.websoso.WSSServer.auth.repository.RefreshTokenRepository;
import org.websoso.WSSServer.auth.client.AppleClient;
import org.websoso.WSSServer.auth.client.AppleIdTokenVerifier;
import org.websoso.WSSServer.auth.client.AppleKeyGenerator;
import org.websoso.WSSServer.auth.service.AppleService;
import org.websoso.WSSServer.auth.client.KakaoService;
import org.websoso.WSSServer.auth.service.TokenService;
import org.websoso.WSSServer.auth.client.dto.KakaoUserInfo;
import org.websoso.WSSServer.dto.user.LoginResponse;
import org.websoso.WSSServer.exception.exception.CustomAuthException;
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
    private final AppleService appleService;
    private final AppleClient appleClient;
    private static final String KAKAO_PREFIX = "kakao";
    private static final String APPLE_PREFIX = "apple";
    private final AppleKeyGenerator appleKeyGenerator;
    private final AppleIdTokenVerifier appleIdTokenVerifier;

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

    @Transactional
    public AuthResponse loginApple(String authorizationCode, String appleToken) {
        // 1. Apple ID Token 검증 (헤더 파싱 + 공개키 조회 + 서명 검증)
        Claims claims = appleIdTokenVerifier.verify(appleToken);

        // 2. 애플 서버에서 Refresh Token 받아오기
        String clientSecret = appleKeyGenerator.createClientSecret();
        AppleTokenResponse appleTokenResponse = appleClient.requestAppleToken(authorizationCode, clientSecret);

        // 3. 유저 정보 추출
        String email = claims.get("email", String.class);
        String userIdentifier = claims.get("sub", String.class);
        String customSocialId = APPLE_PREFIX + "_" + userIdentifier;
        String defaultNickname = APPLE_PREFIX.charAt(0) + "*" + userIdentifier.substring(7, 15);

        // 4. 유저 처리
        User user = userService.getOrCreateAppleUser(customSocialId, email, defaultNickname);

        // 5. 애플 Refresh Token 저장
        appleService.upsertRefreshToken(user, appleTokenResponse.getRefreshToken());

        // 6. Access / Refresh Token 생성
        CustomAuthenticationToken customAuthenticationToken = CustomAuthenticationToken.create(user.getUserId());
        String accessToken = jwtProvider.generateAccessToken(customAuthenticationToken);
        String refreshToken = jwtProvider.generateRefreshToken(customAuthenticationToken);

        // 7. Refresh Token 저장
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

    @Transactional
    public void logout(User user, LogoutRequest request) {
        refreshTokenRepository.findByRefreshToken(request.refreshToken())
                .ifPresent(refreshTokenRepository::delete);

        userDeviceRepository.deleteByUserAndDeviceIdentifier(user, request.deviceIdentifier());

        if (user.getSocialId().startsWith(KAKAO_PREFIX)) {
            kakaoService.kakaoLogout(user);
        }
    }
}
