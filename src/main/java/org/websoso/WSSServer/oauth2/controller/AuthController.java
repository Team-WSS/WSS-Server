package org.websoso.WSSServer.oauth2.controller;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.config.jwt.JwtProvider;
import org.websoso.WSSServer.dto.auth.AppleIdUpdateRequest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.dto.auth.AppleLoginRequest;
import org.websoso.WSSServer.dto.auth.AuthResponse;
import org.websoso.WSSServer.dto.auth.LogoutRequest;
import org.websoso.WSSServer.dto.auth.ReissueRequest;
import org.websoso.WSSServer.dto.auth.ReissueResponse;
import org.websoso.WSSServer.dto.user.WithdrawalRequest;
import org.websoso.WSSServer.oauth2.service.AppleService;
import org.websoso.WSSServer.oauth2.service.KakaoService;
import org.websoso.WSSServer.application.AccountApplication;
import org.websoso.WSSServer.application.AuthApplication;
import org.websoso.WSSServer.user.repository.UserRepository;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplication authApplication;
    private final AppleService appleService;
    private final AccountApplication accountApplication;

    // 애플 로그인 토큰 만료로 인한 임시 의존성 주입
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    // Access Token 재발급
    @PostMapping("/reissue")
    public ResponseEntity<ReissueResponse> reissue(@RequestBody ReissueRequest reissueRequest) {
        return ResponseEntity
                .status(OK)
                .body(authApplication.reissue(reissueRequest.refreshToken()));
    }

    @PostMapping("/auth/login/kakao")
    public ResponseEntity<AuthResponse> loginByKakao(@RequestHeader("Kakao-Access-Token") String kakaoAccessToken) {
        return ResponseEntity
                .status(OK)
                .body(authApplication.loginKakao(kakaoAccessToken));
    }

    @PostMapping("/auth/login/apple")
    public ResponseEntity<AuthResponse> loginByApple(@Valid @RequestBody AppleLoginRequest request) {
        return ResponseEntity
                .status(OK)
                .body(appleService.getUserInfoFromApple(request));
    }

    @PatchMapping("/auth/apple/sync")
    public ResponseEntity<Void> updateAppleSocialId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken,
            @Valid @RequestBody AppleIdUpdateRequest request
    ) {
        Long userId = jwtProvider.getUserIdFromToken(bearerToken);

        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            if (!request.authorizationCode().isBlank() && !request.idToken().isBlank()) {
                appleService.syncSocialId(user, request);
            }
        }

        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }


    @PostMapping("/auth/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User user,
                                       @Valid @RequestBody LogoutRequest request) {
        authApplication.logout(user, request);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @PostMapping("/auth/withdraw")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> withdrawUser(@AuthenticationPrincipal User user,
                                             @Valid @RequestBody WithdrawalRequest withdrawalRequest) {
        accountApplication.withdrawUser(user, withdrawalRequest);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }
}
