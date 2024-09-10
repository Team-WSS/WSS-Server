package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomAuthError.EXPIRED_REFRESH_TOKEN;
import static org.websoso.WSSServer.exception.error.CustomAuthError.INVALID_TOKEN;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.config.jwt.JWTUtil;
import org.websoso.WSSServer.config.jwt.JwtProvider;
import org.websoso.WSSServer.config.jwt.JwtValidationType;
import org.websoso.WSSServer.config.jwt.UserAuthentication;
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

    public ReissueResponse reissue(String refreshToken) {
        JwtValidationType validationResult = jwtUtil.validateJWT(refreshToken);
        String tokenType = jwtUtil.getTokenTypeFromJwt(refreshToken);
        if ("refresh".equals(tokenType)) {
            if (validationResult == JwtValidationType.VALID_TOKEN) {
                Long userId = jwtUtil.getUserIdFromJwt(refreshToken);
                UserAuthentication userAuthentication = new UserAuthentication(userId, null, null);
                String newAccessToken = jwtProvider.generateAccessToken(userAuthentication);
                String newRefreshToken = jwtProvider.generateRefreshToken(userAuthentication);
                return ReissueResponse.of(newAccessToken, newRefreshToken);
            } else if (validationResult == JwtValidationType.EXPIRED_TOKEN) {
                throw new CustomAuthException(EXPIRED_REFRESH_TOKEN, "given token is expired refresh token.");
            }
        }
        throw new CustomAuthException(INVALID_TOKEN, "given token is invalid token.");
    }
}
