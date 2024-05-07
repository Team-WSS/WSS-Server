package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.dto.User.NicknameValidation;
import org.websoso.WSSServer.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/nickname/check")
    public ResponseEntity<NicknameValidation> checkNicknameAvailability(
            @RequestParam("nickname") String nickname) {
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
}
