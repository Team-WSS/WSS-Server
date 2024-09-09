package org.websoso.WSSServer.apple.controller;

import static org.springframework.http.HttpStatus.OK;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.apple.service.AppleService;
import org.websoso.WSSServer.dto.user.LoginResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/login")
public class AppleController {

    private final AppleService appleService;

    @PostMapping("/callback")
    public ResponseEntity<LoginResponse> callback(HttpServletRequest request) throws Exception {
        return ResponseEntity
                .status(OK)
                .body(appleService.getAppleInfo(request.getParameter("code")));
    }

}
