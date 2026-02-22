package org.websoso.WSSServer.application;

import static org.websoso.WSSServer.exception.error.CustomAuthError.INVALID_TOKEN;

import io.jsonwebtoken.Claims;
import java.security.PublicKey;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.config.jwt.CustomAuthenticationToken;
import org.websoso.WSSServer.config.jwt.JWTUtil;
import org.websoso.WSSServer.config.jwt.JwtProvider;
import org.websoso.WSSServer.config.jwt.JwtValidationType;
import org.websoso.WSSServer.dto.auth.ApplePublicKeys;
import org.websoso.WSSServer.dto.auth.AppleTokenResponse;
import org.websoso.WSSServer.dto.auth.AuthResponse;
import org.websoso.WSSServer.dto.auth.LogoutRequest;
import org.websoso.WSSServer.dto.auth.ReissueResponse;
import org.websoso.WSSServer.dto.user.LoginResponse;
import org.websoso.WSSServer.exception.exception.CustomAuthException;
import org.websoso.WSSServer.oauth2.domain.UserAppleToken;
import org.websoso.WSSServer.oauth2.dto.KakaoUserInfo;
import org.websoso.WSSServer.oauth2.service.AppleClient;
import org.websoso.WSSServer.oauth2.service.AppleKeyGenerator;
import org.websoso.WSSServer.oauth2.service.AppleService;
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
    private final AppleService appleService;
    private final AppleClient appleClient;
    private static final String KAKAO_PREFIX = "kakao";
    private static final String APPLE_PREFIX = "apple";
    private final AppleKeyGenerator appleKeyGenerator;

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
        // 1. 공개키 가져오기 & 헤더 파싱
        ApplePublicKeys applePublicKeys = appleClient.getApplePublicKeys();
        Map<String, String> headers = jwtProvider.parseAppleTokenHeader(appleToken);

        // 2. 공개키 생성 및 검증 (서명 확인)
        PublicKey publicKey = appleKeyGenerator.generatePublicKey(headers, applePublicKeys);
        Claims claims = jwtProvider.extractClaims(appleToken, publicKey);

        // 3. 애플 서버에서 Refresh Token 받아오기
        String clientSecret = appleKeyGenerator.createClientSecret();
        AppleTokenResponse appleTokenResponse = appleClient.requestAppleToken(authorizationCode, clientSecret);

        // 4. 유저 정보 추출
        String email = claims.get("email", String.class);
        String userIdentifier = claims.get("sub", String.class);
        String customSocialId = APPLE_PREFIX + "_" + userIdentifier;
        String defaultNickname = APPLE_PREFIX.charAt(0) + "*" + userIdentifier.substring(7, 15);

        // 5. 유저 처리
        User user = userService.getOrCreateAppleUser(customSocialId, email, defaultNickname);

        // 6. 애플 Refresh Token 저장
        appleService.upsertRefreshToken(user, appleTokenResponse.getRefreshToken());

        // 7. Access / Refresh Token 생성
        CustomAuthenticationToken customAuthenticationToken = CustomAuthenticationToken.create(user.getUserId());
        String accessToken = jwtProvider.generateAccessToken(customAuthenticationToken);
        String refreshToken = jwtProvider.generateRefreshToken(customAuthenticationToken);

        // 8. Refresh Token 저장
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
