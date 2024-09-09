package org.websoso.WSSServer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.config.jwt.JwtProvider;
import org.websoso.WSSServer.config.jwt.JwtValidationType;
import org.websoso.WSSServer.config.jwt.UserAuthentication;
import org.websoso.WSSServer.dto.auth.ReissueResponse;

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
        }
    }
}
