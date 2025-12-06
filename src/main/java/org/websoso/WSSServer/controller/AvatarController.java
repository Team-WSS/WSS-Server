package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.dto.avatar.AvatarsGetResponse;
import org.websoso.WSSServer.service.AvatarService;

@RequestMapping("/avatars")
@RestController
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AvatarsGetResponse> getAvatarList(@AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(avatarService.getAvatarList(user));
    }
}
