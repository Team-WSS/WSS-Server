package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomAuthError.EXPIRED_REFRESH_TOKEN;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.config.jwt.JwtProvider;
import org.websoso.WSSServer.config.jwt.JwtValidationType;
import org.websoso.WSSServer.config.jwt.UserAuthentication;
import org.websoso.WSSServer.dto.auth.ReissueResponse;
import org.websoso.WSSServer.exception.exception.CustomAuthException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final JwtProvider jwtProvider;

    public ReissueResponse reissue(String refreshToken) {
        JwtValidationType validationResult = jwtProvider.validateJWT(refreshToken);

        if (validationResult == JwtValidationType.VALID_TOKEN) {
            Long userId = jwtProvider.getUserIdFromJwt(refreshToken);
            UserAuthentication userAuthentication = new UserAuthentication(userId, null, null);
            String newAccessToken = jwtProvider.generateAccessToken(userAuthentication);
            return ReissueResponse.of(newAccessToken);
        } else if (validationResult == JwtValidationType.EXPIRED_TOKEN) {
            throw new CustomAuthException(EXPIRED_REFRESH_TOKEN, "given token is expired refresh token.");
        }
    }
}
