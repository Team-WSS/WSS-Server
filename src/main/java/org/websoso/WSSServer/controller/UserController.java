package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.dto.User.NicknameValidation;
import org.websoso.WSSServer.exception.common.ErrorResult;
import org.websoso.WSSServer.exception.user.UserErrorCode;
import org.websoso.WSSServer.exception.user.exception.InvalidNicknameException;
import org.websoso.WSSServer.service.UserService;
import org.websoso.WSSServer.validation.NicknameConstraint;

@Slf4j
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

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(InvalidNicknameException.class)
    public ErrorResult InvalidNicknameExceptionHandler(InvalidNicknameException e) {
        log.error("[InvalidNicknameException] exception ", e);
        UserErrorCode userErrorCode = e.getUserErrorCode();
        return new ErrorResult(userErrorCode.getCode(), userErrorCode.getDescription());
    }
}