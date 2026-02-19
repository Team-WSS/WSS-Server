package org.websoso.WSSServer.oauth2.service;

import static org.websoso.WSSServer.exception.error.CustomAuthError.INVALID_TOKEN;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.exception.exception.CustomAuthException;
import org.websoso.WSSServer.oauth2.domain.RefreshToken;
import org.websoso.WSSServer.oauth2.repository.RefreshTokenRepository;
import org.websoso.WSSServer.user.domain.User;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;


    // 기존 리프레시 토큰을 삭제하고, 새로운 리프레시 토큰을 저장한다.
    @Transactional
    public void rotateRefreshToken(RefreshToken oldToken, String newTokenValue, Long userId) {
        refreshTokenRepository.delete(oldToken);
        refreshTokenRepository.save(new RefreshToken(newTokenValue, userId));
    }

    // 리프레시 토큰을 저장한다.
    @Transactional
    public void saveRefreshToken(User user, String refreshToken) {
        refreshTokenRepository.save(new RefreshToken(refreshToken, user.getUserId()));
    }

    // 리프레시 토큰을 조회한다.
    @Transactional(readOnly = true)
    public RefreshToken findRefreshTokenOrThrow(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new CustomAuthException(INVALID_TOKEN, "given token is invalid token for reissue"));
    }
}
