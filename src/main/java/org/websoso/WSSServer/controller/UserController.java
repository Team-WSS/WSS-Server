package org.websoso.WSSServer.controller;

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
import org.websoso.WSSServer.dto.user.EmailGetResponse;
import org.websoso.WSSServer.service.UserService;
import org.websoso.WSSServer.validation.NicknameConstraint;

@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/nickname/check")
    public ResponseEntity<NicknameValidation> checkNicknameAvailability(
            @RequestParam("nickname")
            @NicknameConstraint String nickname) {
        return ResponseEntity
                .status(OK)
                .body(userService.isNicknameAvailable(nickname));
    }

    @GetMapping("/email")
    public ResponseEntity<EmailGetResponse> getEmail(Principal principal) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(userService.getEmail(user));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody String userId) {
        LoginResponse response = userService.login(Long.valueOf(userId));
        return ResponseEntity
                .status(OK)
                .body(response);
    }
}
