package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.dto.auth.AuthResponse;
import org.websoso.WSSServer.dto.auth.ReissueRequest;
import org.websoso.WSSServer.dto.auth.ReissueResponse;
import org.websoso.WSSServer.oauth2.service.KakaoService;
import org.websoso.WSSServer.service.AuthService;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final KakaoService kakaoService;

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
}
