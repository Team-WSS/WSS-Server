package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.user.EditProfileStatusRequest;
import org.websoso.WSSServer.dto.user.EmailGetResponse;
import org.websoso.WSSServer.dto.user.LoginResponse;
import org.websoso.WSSServer.dto.user.MyProfileResponse;
import org.websoso.WSSServer.dto.user.NicknameValidation;
import org.websoso.WSSServer.dto.user.ProfileStatusResponse;
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

    @GetMapping("/profile-status")
    public ResponseEntity<ProfileStatusResponse> getProfileStatus(Principal principal) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(userService.getProfileStatus(user));
    }

    @PatchMapping("/profile-status")
    public ResponseEntity<Void> editProfileStatus(Principal principal,
                                                  @Valid @RequestBody EditProfileStatusRequest editProfileStatusRequest) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        userService.editProfileStatus(user, editProfileStatusRequest);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody String userId) {
        LoginResponse response = userService.login(Long.valueOf(userId));
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @GetMapping("/my-profile")
    public ResponseEntity<MyProfileResponse> getMyProfileInfo(Principal principal) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(userService.getMyProfileInfo(user));
    }
}
