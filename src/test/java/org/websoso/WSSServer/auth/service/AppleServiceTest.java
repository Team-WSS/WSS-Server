package org.websoso.WSSServer.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.TOKEN_REQUEST_FAILED;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.TOKEN_REVOKE_FAILED;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.USER_APPLE_REFRESH_TOKEN_NOT_FOUND;

import io.jsonwebtoken.Claims;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
class AppleServiceTest {

    private static final String AUTHORIZATION_CODE = "apple-authorization-code";
    private static final String ID_TOKEN = "apple-id-token";
    private static final String CLIENT_SECRET = "apple-client-secret";
    private static final String APPLE_REFRESH_TOKEN = "apple-refresh-token";
    private static final String USER_IDENTIFIER = "001234.abcdefghijklmn.1234";
    private static final String EMAIL = "websoso@websoso.org";

    @Mock
    private UserAppleTokenRepository userAppleTokenRepository;

    @Mock
    private AppleClient appleClient;

    @Mock
    private AppleKeyGenerator appleKeyGenerator;

    @Mock
    private AppleIdTokenVerifier appleIdTokenVerifier;

    @Mock
    private Claims claims;

    @Mock
    private User user;

    @InjectMocks
    private AppleService appleService;

    @DisplayName("Apple 인증에 성공하면 ID Token 검증 결과와 Apple Refresh Token을 담아 반환한다")
    @Test
    void authenticate_success() {
        given(appleIdTokenVerifier.verify(ID_TOKEN)).willReturn(claims);
        given(claims.get("sub", String.class)).willReturn(USER_IDENTIFIER);
        given(claims.get("email", String.class)).willReturn(EMAIL);
        given(appleKeyGenerator.createClientSecret()).willReturn(CLIENT_SECRET);
        given(appleClient.requestAppleToken(AUTHORIZATION_CODE, CLIENT_SECRET))
                .willReturn(appleTokenResponse());

        AppleAuthResult result = appleService.authenticate(AUTHORIZATION_CODE, ID_TOKEN);

        assertThat(result.userIdentifier()).isEqualTo(USER_IDENTIFIER);
        assertThat(result.email()).isEqualTo(EMAIL);
        assertThat(result.appleRefreshToken()).isEqualTo(APPLE_REFRESH_TOKEN);
    }

    @DisplayName("Apple 인증 중 토큰 교환이 실패하면 예외가 그대로 전파된다")
    @Test
    void authenticate_tokenRequestFailed() {
        given(appleIdTokenVerifier.verify(ID_TOKEN)).willReturn(claims);
        given(appleKeyGenerator.createClientSecret()).willReturn(CLIENT_SECRET);
        given(appleClient.requestAppleToken(AUTHORIZATION_CODE, CLIENT_SECRET))
                .willThrow(new CustomAppleLoginException(TOKEN_REQUEST_FAILED, "apple token request failed"));

        assertThatThrownBy(() -> appleService.authenticate(AUTHORIZATION_CODE, ID_TOKEN))
                .isInstanceOf(CustomAppleLoginException.class)
                .extracting(throwable -> ((CustomAppleLoginException) throwable).getICustomError())
                .isEqualTo(TOKEN_REQUEST_FAILED);
    }

    @DisplayName("Apple 연결 해제에 성공하면 토큰 폐기를 요청하고 저장된 Apple Refresh Token을 삭제한다")
    @Test
    void unlinkFromApple_success() {
        UserAppleToken userAppleToken = UserAppleToken.create(user, APPLE_REFRESH_TOKEN);
        given(userAppleTokenRepository.findByUser(user)).willReturn(Optional.of(userAppleToken));
        given(appleKeyGenerator.createClientSecret()).willReturn(CLIENT_SECRET);

        appleService.unlinkFromApple(user);

        then(appleClient).should().revokeAppleToken(CLIENT_SECRET, APPLE_REFRESH_TOKEN);
        then(userAppleTokenRepository).should().delete(userAppleToken);
    }

    @DisplayName("저장된 Apple Refresh Token이 없으면 연결 해제 시 예외가 발생한다")
    @Test
    void unlinkFromApple_tokenNotFound() {
        given(userAppleTokenRepository.findByUser(user)).willReturn(Optional.empty());

        assertThatThrownBy(() -> appleService.unlinkFromApple(user))
                .isInstanceOf(CustomAppleLoginException.class)
                .extracting(throwable -> ((CustomAppleLoginException) throwable).getICustomError())
                .isEqualTo(USER_APPLE_REFRESH_TOKEN_NOT_FOUND);

        then(appleClient).should(never()).revokeAppleToken(CLIENT_SECRET, APPLE_REFRESH_TOKEN);
    }

    @DisplayName("토큰 폐기 요청이 실패하면 저장된 Apple Refresh Token을 삭제하지 않는다")
    @Test
    void unlinkFromApple_revokeFailed() {
        UserAppleToken userAppleToken = UserAppleToken.create(user, APPLE_REFRESH_TOKEN);
        given(userAppleTokenRepository.findByUser(user)).willReturn(Optional.of(userAppleToken));
        given(appleKeyGenerator.createClientSecret()).willReturn(CLIENT_SECRET);
        doThrow(new CustomAppleLoginException(TOKEN_REVOKE_FAILED, "apple token revoke failed"))
                .when(appleClient).revokeAppleToken(CLIENT_SECRET, APPLE_REFRESH_TOKEN);

        assertThatThrownBy(() -> appleService.unlinkFromApple(user))
                .isInstanceOf(CustomAppleLoginException.class)
                .extracting(throwable -> ((CustomAppleLoginException) throwable).getICustomError())
                .isEqualTo(TOKEN_REVOKE_FAILED);

        then(userAppleTokenRepository).should(never()).delete(userAppleToken);
    }

    @DisplayName("저장된 Apple Refresh Token이 없으면 새로 저장한다")
    @Test
    void upsertRefreshToken_saveWhenAbsent() {
        given(userAppleTokenRepository.findByUser(user)).willReturn(Optional.empty());

        appleService.upsertRefreshToken(user, APPLE_REFRESH_TOKEN);

        then(userAppleTokenRepository).should().save(any(UserAppleToken.class));
    }

    @DisplayName("저장된 Apple Refresh Token이 있으면 값을 갱신한다")
    @Test
    void upsertRefreshToken_updateWhenPresent() {
        UserAppleToken userAppleToken = UserAppleToken.create(user, "old-refresh-token");
        given(userAppleTokenRepository.findByUser(user)).willReturn(Optional.of(userAppleToken));

        appleService.upsertRefreshToken(user, APPLE_REFRESH_TOKEN);

        assertThat(userAppleToken.getAppleRefreshToken()).isEqualTo(APPLE_REFRESH_TOKEN);
        then(userAppleTokenRepository).should(never()).save(any(UserAppleToken.class));
    }

    @DisplayName("socialId 동기화는 Apple 인증 결과로 socialId와 Apple Refresh Token을 갱신한다")
    @Test
    void syncSocialId_updatesSocialIdAndRefreshToken() {
        UserAppleToken userAppleToken = UserAppleToken.create(user, "old-refresh-token");
        given(userAppleTokenRepository.findByUser(user)).willReturn(Optional.of(userAppleToken));
        given(appleIdTokenVerifier.verify(ID_TOKEN)).willReturn(claims);
        given(claims.get("sub", String.class)).willReturn(USER_IDENTIFIER);
        given(appleKeyGenerator.createClientSecret()).willReturn(CLIENT_SECRET);
        given(appleClient.requestAppleToken(AUTHORIZATION_CODE, CLIENT_SECRET))
                .willReturn(appleTokenResponse());

        appleService.syncSocialId(user, new AppleIdUpdateRequest(AUTHORIZATION_CODE, ID_TOKEN));

        then(user).should().syncSocialId("apple_" + USER_IDENTIFIER);
        assertThat(userAppleToken.getAppleRefreshToken()).isEqualTo(APPLE_REFRESH_TOKEN);
    }

    @DisplayName("저장된 Apple Refresh Token이 없으면 socialId 동기화는 아무것도 하지 않는다")
    @Test
    void syncSocialId_doesNothingWhenTokenNotFound() {
        given(userAppleTokenRepository.findByUser(user)).willReturn(Optional.empty());

        appleService.syncSocialId(user, new AppleIdUpdateRequest(AUTHORIZATION_CODE, ID_TOKEN));

        then(appleIdTokenVerifier).should(never()).verify(ID_TOKEN);
        then(appleClient).should(never()).requestAppleToken(AUTHORIZATION_CODE, CLIENT_SECRET);
    }

    private AppleTokenResponse appleTokenResponse() {
        return new AppleTokenResponse("apple-access-token", "3600", ID_TOKEN, APPLE_REFRESH_TOKEN, "bearer", null);
    }
}
