package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomAuthError.INVALID_TOKEN;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.config.jwt.JWTUtil;
import org.websoso.WSSServer.config.jwt.JwtProvider;
import org.websoso.WSSServer.config.jwt.JwtValidationType;
import org.websoso.WSSServer.config.jwt.UserAuthentication;
import org.websoso.WSSServer.domain.RefreshToken;
import org.websoso.WSSServer.dto.auth.ReissueResponse;
import org.websoso.WSSServer.exception.exception.CustomAuthException;
import org.websoso.WSSServer.repository.RefreshTokenRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final JwtProvider jwtProvider;
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    public ReissueResponse reissue(String refreshToken) {
        RefreshToken storedRefreshToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new CustomAuthException(INVALID_TOKEN, "given token is invalid token for reissue"));

        if (jwtUtil.validateJWT(refreshToken) != JwtValidationType.VALID_REFRESH) {
            throw new CustomAuthException(INVALID_TOKEN, "given token is invalid token for reissue");
        }

        refreshTokenRepository.delete(storedRefreshToken);
        Long userId = jwtUtil.getUserIdFromJwt(refreshToken);
        userService.getUserOrException(userId);

        UserAuthentication userAuthentication = new UserAuthentication(userId, null, null);
        String newAccessToken = jwtProvider.generateAccessToken(userAuthentication);
        String newRefreshToken = jwtProvider.generateRefreshToken(userAuthentication);
        refreshTokenRepository.save(new RefreshToken(newRefreshToken, userId));

        return ReissueResponse.of(newAccessToken, newRefreshToken);
    }
}
