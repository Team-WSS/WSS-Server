package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.dto.auth.ReissueRequest;
import org.websoso.WSSServer.dto.auth.ReissueResponse;
import org.websoso.WSSServer.service.AuthService;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/reissue")
    public ResponseEntity<ReissueResponse> reissue(@RequestBody ReissueRequest reissueRequest) {
        String refreshToken = reissueRequest.refreshToken();
        return ResponseEntity
                .status(OK)
                .body(authService.reissue(refreshToken));
    }
}
