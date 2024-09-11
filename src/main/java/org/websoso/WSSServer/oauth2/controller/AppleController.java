package org.websoso.WSSServer.oauth2.controller;

import static org.springframework.http.HttpStatus.OK;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.dto.user.LoginResponse;
import org.websoso.WSSServer.oauth2.service.AppleService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/login")
public class AppleController {

    private final AppleService appleService;

    @PostMapping("/callback")
    public ResponseEntity<LoginResponse> callback(HttpServletRequest request) {
        return ResponseEntity
                .status(OK)
                .body(appleService.getAppleInfo(request.getParameter("code")));
    }
}
