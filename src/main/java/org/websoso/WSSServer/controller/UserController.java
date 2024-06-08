package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.User.LoginResponse;
import org.websoso.WSSServer.dto.User.NicknameValidation;
import org.websoso.WSSServer.service.BlockService;
import org.websoso.WSSServer.service.UserService;
import org.websoso.WSSServer.validation.NicknameConstraint;

@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BlockService blockService;

    @GetMapping("/nickname/check")
    public ResponseEntity<NicknameValidation> checkNicknameAvailability(
            @RequestParam("nickname")
            @NicknameConstraint String nickname) {
        NicknameValidation nicknameValidation = userService.isNicknameAvailable(nickname);
        if (nicknameValidation.isDuplicated()) {
            return ResponseEntity
                    .status(CONFLICT)
                    .body(nicknameValidation);
        }
        return ResponseEntity
                .status(OK)
                .body(nicknameValidation);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody String userId) {
        LoginResponse response = userService.login(Long.valueOf(userId));
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @PostMapping("/blocks")
    public ResponseEntity<Void> block(Principal principal,
                                      @RequestParam("userId") Long blockedId) {
        User blocker = userService.getUserOrException(Long.valueOf(principal.getName()));
        blockService.block(blocker, blockedId);
        return ResponseEntity
                .status(CREATED)
                .build();
    }
}
