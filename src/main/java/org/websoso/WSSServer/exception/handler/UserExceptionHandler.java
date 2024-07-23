package org.websoso.WSSServer.exception.handler;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.websoso.WSSServer.exception.error.CustomUserError.DUPLICATED_NICKNAME;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.websoso.WSSServer.controller.UserController;
import org.websoso.WSSServer.exception.common.ErrorResult;

@Slf4j
@RestControllerAdvice(assignableTypes = UserController.class)
public class UserExceptionHandler {

    //TODO DataIntegrityViolationException이 unique 제약조건을 만족못할 때 뿐만 아니라 db의 무결성 제약조건이 위반되는 경우 모두 발생함
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResult> DataIntegrityViolationExceptionHandler(DataIntegrityViolationException e) {
        log.error("[DataIntegrityViolationException] exception ", e);
        return ResponseEntity
                .status(CONFLICT)
                .body(new ErrorResult(DUPLICATED_NICKNAME.getCode(), DUPLICATED_NICKNAME.getDescription()));
    }
}
