package org.websoso.WSSServer.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.websoso.WSSServer.exception.category.CategoryErrorCode;
import org.websoso.WSSServer.exception.category.exeption.InvalidCategoryException;
import org.websoso.WSSServer.exception.common.ErrorResult;
import org.websoso.WSSServer.exception.user.UserErrorCode;
import org.websoso.WSSServer.exception.user.exception.InvalidNicknameException;
import org.websoso.WSSServer.exception.user.exception.InvalidUserException;

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

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(InvalidUserException.class)
    public ErrorResult InvalidUserExceptionHandler(InvalidUserException e) {
        log.error("[InvalidUserException] exception ", e);
        UserErrorCode userErrorCode = e.getUserErrorCode();
        return new ErrorResult(userErrorCode.getCode(), userErrorCode.getDescription());
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(InvalidCategoryException.class)
    public ErrorResult InvalidCategoryExceptionHandler(InvalidCategoryException e) {
        log.error("[InvalidCategoryException] exception ", e);
        CategoryErrorCode categoryErrorCode = e.getCategoryErrorCode();
        return new ErrorResult(categoryErrorCode.getCode(), categoryErrorCode.getDescription());
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResult InvalidFeedExceptionHandler(MethodArgumentNotValidException e) {
        log.error("[MethodArgumentNotValidException] exception ", e);
        return new ErrorResult("BAD REQUEST", e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }
}