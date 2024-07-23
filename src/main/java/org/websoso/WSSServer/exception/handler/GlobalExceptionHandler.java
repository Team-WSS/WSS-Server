package org.websoso.WSSServer.exception.handler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.websoso.WSSServer.exception.error.CustomUserError.DUPLICATED_NICKNAME;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.common.ErrorResult;
import org.websoso.WSSServer.exception.common.ICustomError;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResult> InvalidRequestBodyExceptionHandler(MethodArgumentNotValidException e) {
        log.error("[MethodArgumentNotValidException] exception ", e);
        HttpStatus httpStatus = HttpStatus.valueOf(e.getStatusCode().value());
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResult(httpStatus.name(),
                        e.getBindingResult().getAllErrors().get(0).getDefaultMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResult> HttpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e) {
        log.error("[HttpMessageNotReadableException] exception ", e);
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new ErrorResult(BAD_REQUEST.name(), "잘못된 JSON 형식입니다."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResult> DataIntegrityViolationExceptionHandler(DataIntegrityViolationException e) {
        log.error("[DataIntegrityViolationException] exception ", e);
        return ResponseEntity
                .status(CONFLICT)
                .body(new ErrorResult(DUPLICATED_NICKNAME.getCode(), DUPLICATED_NICKNAME.getDescription()));
    }

    @ExceptionHandler(AbstractCustomException.class)
    public ResponseEntity<ErrorResult> CustomExceptionHandler(AbstractCustomException e) {
        log.error("[{}] exception ", e.getClass().getSimpleName(), e);
        ICustomError iCustomError = e.getICustomError();
        return ResponseEntity
                .status(iCustomError.getStatusCode())
                .body(new ErrorResult(iCustomError.getCode(), iCustomError.getDescription()));
    }
}
