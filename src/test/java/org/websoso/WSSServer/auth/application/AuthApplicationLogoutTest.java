package org.websoso.WSSServer.auth.application;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.auth.client.KakaoClient;
import org.websoso.WSSServer.auth.controller.dto.LogoutRequest;
import org.websoso.WSSServer.auth.jwt.JWTUtil;
import org.websoso.WSSServer.auth.jwt.JwtProvider;
import org.websoso.WSSServer.auth.service.AppleService;
import org.websoso.WSSServer.auth.service.RefreshTokenLockService;
import org.websoso.WSSServer.auth.service.TokenService;
import org.websoso.WSSServer.notification.service.UserDeviceService;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthApplicationLogoutTest {

    private static final String REFRESH_TOKEN = "refresh-token-value";
    private static final String DEVICE_IDENTIFIER = "device-identifier-value";

    @Mock
    private TokenService tokenService;

    @Mock
    private RefreshTokenLockService refreshTokenLockService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private UserDeviceService userDeviceService;

    @Mock
    private UserService userService;

    @Mock
    private KakaoClient kakaoClient;

    @Mock
    private AppleService appleService;

    @Mock
    private User user;

    private AuthApplication authApplication;

    @BeforeEach
    void setUp() {
        authApplication = new AuthApplication(tokenService, refreshTokenLockService, jwtProvider, jwtUtil,
                userDeviceService, userService, kakaoClient, appleService);
    }

    @DisplayName("로그아웃하면 요청에 담긴 리프레시 토큰을 삭제하고 UserDeviceService로 디바이스 식별자를 삭제한다")
    @Test
    void logout_deletesRefreshTokenAndDeviceIdentifier() {
        given(user.getSocialId()).willReturn("apple_1234567890");

        authApplication.logout(user, new LogoutRequest(REFRESH_TOKEN, DEVICE_IDENTIFIER));

        then(tokenService).should().deleteRefreshToken(REFRESH_TOKEN);
        then(userDeviceService).should().deleteDeviceIdentifier(user, DEVICE_IDENTIFIER);
    }

    @DisplayName("카카오 사용자가 로그아웃해도 디바이스 식별자 삭제는 UserDeviceService를 통해 수행한다")
    @Test
    void logout_kakaoUser_deletesDeviceIdentifierThroughUserDeviceService() {
        given(user.getSocialId()).willReturn("kakao_1234567890");

        authApplication.logout(user, new LogoutRequest(REFRESH_TOKEN, DEVICE_IDENTIFIER));

        then(userDeviceService).should().deleteDeviceIdentifier(user, DEVICE_IDENTIFIER);
        then(kakaoClient).should().logout("1234567890");
    }
}
