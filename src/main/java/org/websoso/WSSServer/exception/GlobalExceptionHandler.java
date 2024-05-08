package org.websoso.WSSServer.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.websoso.WSSServer.exception.user.UserErrorCode;
import org.websoso.WSSServer.exception.common.ErrorResult;
import org.websoso.WSSServer.exception.user.exception.InvalidNicknameException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(InvalidNicknameException.class)
    public ErrorResult InvalidNicknameExceptionHandler(InvalidNicknameException e) {
        log.error("[InvalidNicknameException] exception ", e);
        UserErrorCode userErrorCode = e.getUserErrorCode();
        return new ErrorResult(userErrorCode.getCode(), userErrorCode.getDescription());
    }
}
