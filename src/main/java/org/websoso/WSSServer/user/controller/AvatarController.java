package org.websoso.WSSServer.user.controller;

import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.dto.avatar.AvatarProfilesGetResponse;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.dto.avatar.AvatarsGetResponse;
import org.websoso.WSSServer.service.AvatarService;

@RequestMapping
@RestController
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;

    @GetMapping("/avatars")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AvatarsGetResponse> getAvatarList(@AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(avatarService.getAvatarList(user));
    }

    @GetMapping("/avatar-profiles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AvatarProfilesGetResponse> getAvatarProfileList(@AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(avatarService.getAvatarProfileList(user));
    }
}
