package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.auth.AppleLoginRequest;
import org.websoso.WSSServer.dto.auth.AuthResponse;
import org.websoso.WSSServer.dto.auth.ReissueRequest;
import org.websoso.WSSServer.dto.auth.ReissueResponse;
import org.websoso.WSSServer.oauth2.service.AppleService;
import org.websoso.WSSServer.oauth2.service.KakaoService;
import org.websoso.WSSServer.service.AuthService;
import org.websoso.WSSServer.service.UserService;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final KakaoService kakaoService;
    private final AppleService appleService;
    private final UserService userService;

    @PostMapping("/reissue")
    public ResponseEntity<ReissueResponse> reissue(@RequestBody ReissueRequest reissueRequest) {
        String refreshToken = reissueRequest.refreshToken();
        return ResponseEntity
                .status(OK)
                .body(authService.reissue(refreshToken));
    }

    @PostMapping("/auth/login/kakao")
    public ResponseEntity<AuthResponse> loginByKakao(@RequestHeader("Kakao-Access-Token") String kakaoAccessToken) {
        return ResponseEntity
                .status(OK)
                .body(kakaoService.getUserInfoFromKakao(kakaoAccessToken));
    }

    @PostMapping("/auth/login/apple")
    public ResponseEntity<AuthResponse> loginByApple(@Valid @RequestBody AppleLoginRequest request) {
        return ResponseEntity
                .status(OK)
                .body(appleService.getUserInfoFromApple(request));
    }

    @PostMapping("/auth/logout/kakao")
    public ResponseEntity<Void> kakaoLogout(Principal principal) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        kakaoService.kakaoLogout(user);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
        // TODO redis에서 refreshToken 삭제
    }
}
