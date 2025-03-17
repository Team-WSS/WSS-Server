package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.avatar.AvatarsGetResponse;
import org.websoso.WSSServer.service.AvatarService;
import org.websoso.WSSServer.service.UserService;

@RequestMapping("/avatars")
@RestController
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;
    private final UserService userService;


    @GetMapping
    public ResponseEntity<AvatarsGetResponse> getAvatarList(@AuthenticationPrincipal User user) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(avatarService.getAvatarList(user));
    }
}
