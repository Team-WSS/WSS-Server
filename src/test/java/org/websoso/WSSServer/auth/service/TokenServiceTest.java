package org.websoso.WSSServer.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.websoso.WSSServer.exception.error.CustomAuthError.INVALID_TOKEN;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.auth.domain.RefreshToken;
import org.websoso.WSSServer.auth.repository.RefreshTokenRepository;
import org.websoso.WSSServer.exception.exception.CustomAuthException;
import org.websoso.WSSServer.user.domain.User;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    private static final Long USER_ID = 1L;
    private static final String REFRESH_TOKEN = "refresh-token-value";

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private User user;

    @DisplayName("리프레시 토큰을 저장한다")
    @Test
    void savesRefreshToken() {
        given(user.getUserId()).willReturn(USER_ID);
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);

        tokenService.saveRefreshToken(user, REFRESH_TOKEN);

        then(refreshTokenRepository).should().save(captor.capture());
        assertThat(captor.getValue().getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
    }

    @DisplayName("저장된 리프레시 토큰을 조회한다")
    @Test
    void findsStoredRefreshToken() {
        RefreshToken stored = new RefreshToken(REFRESH_TOKEN, USER_ID);
        given(refreshTokenRepository.findByRefreshToken(REFRESH_TOKEN)).willReturn(Optional.of(stored));

        RefreshToken result = tokenService.findRefreshTokenOrThrow(REFRESH_TOKEN);

        assertThat(result).isSameAs(stored);
    }

    @DisplayName("저장되지 않은 리프레시 토큰을 조회하면 예외가 발생한다")
    @Test
    void rejectsUnknownRefreshToken() {
        given(refreshTokenRepository.findByRefreshToken(REFRESH_TOKEN)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tokenService.findRefreshTokenOrThrow(REFRESH_TOKEN))
                .isInstanceOf(CustomAuthException.class)
                .extracting(throwable -> ((CustomAuthException) throwable).getICustomError())
                .isEqualTo(INVALID_TOKEN);
    }

    @DisplayName("기존 리프레시 토큰을 삭제하고 새 리프레시 토큰을 저장한다")
    @Test
    void rotatesRefreshToken() {
        RefreshToken oldToken = new RefreshToken(REFRESH_TOKEN, USER_ID);
        String newTokenValue = "new-refresh-token-value";
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);

        tokenService.rotateRefreshToken(oldToken, newTokenValue, USER_ID);

        then(refreshTokenRepository).should().delete(oldToken);
        then(refreshTokenRepository).should().save(captor.capture());
        assertThat(captor.getValue().getRefreshToken()).isEqualTo(newTokenValue);
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
    }

    @DisplayName("저장된 리프레시 토큰을 삭제한다")
    @Test
    void deletesStoredRefreshToken() {
        RefreshToken stored = new RefreshToken(REFRESH_TOKEN, USER_ID);
        given(refreshTokenRepository.findByRefreshToken(REFRESH_TOKEN)).willReturn(Optional.of(stored));

        tokenService.deleteRefreshToken(REFRESH_TOKEN);

        then(refreshTokenRepository).should().delete(stored);
    }

    @DisplayName("저장되지 않은 리프레시 토큰을 삭제해도 예외 없이 무시한다")
    @Test
    void ignoresDeletingUnknownRefreshToken() {
        given(refreshTokenRepository.findByRefreshToken(REFRESH_TOKEN)).willReturn(Optional.empty());

        tokenService.deleteRefreshToken(REFRESH_TOKEN);

        then(refreshTokenRepository).should(never()).delete(ArgumentMatchers.any());
    }
}
