package org.websoso.WSSServer.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.websoso.WSSServer.exception.common.ErrorResult;
import org.websoso.WSSServer.exception.error.CustomAttractivePointError;
import org.websoso.WSSServer.exception.error.CustomAvatarError;
import org.websoso.WSSServer.exception.error.CustomBlockError;
import org.websoso.WSSServer.exception.error.CustomCategoryError;
import org.websoso.WSSServer.exception.error.CustomFeedError;
import org.websoso.WSSServer.exception.error.CustomKeywordError;
import org.websoso.WSSServer.exception.error.CustomNoticeError;
import org.websoso.WSSServer.exception.error.CustomNovelError;
import org.websoso.WSSServer.exception.error.CustomNovelStatisticsError;
import org.websoso.WSSServer.exception.error.CustomUserError;
import org.websoso.WSSServer.exception.error.CustomUserNovelError;
import org.websoso.WSSServer.exception.error.CustomUserStatisticsError;
import org.websoso.WSSServer.exception.exception.CustomAttractivePointException;
import org.websoso.WSSServer.exception.exception.CustomAvatarException;
import org.websoso.WSSServer.exception.exception.CustomBlockException;
import org.websoso.WSSServer.exception.exception.CustomCategoryException;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.exception.exception.CustomKeywordException;
import org.websoso.WSSServer.exception.exception.CustomNoticeException;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.exception.exception.CustomNovelStatisticsException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.exception.exception.CustomUserNovelException;
import org.websoso.WSSServer.exception.exception.CustomUserStatisticsException;

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

    @ExceptionHandler(CustomAttractivePointException.class)
    public ResponseEntity<ErrorResult> CustomAttractivePointExceptionHandler(CustomAttractivePointException e) {
        log.error("[CustomAttractivePointException] exception ", e);
        CustomAttractivePointError customAttractivePointError = e.getCustomAttractivePointError();
        return ResponseEntity
                .status(customAttractivePointError.getStatusCode())
                .body(new ErrorResult(customAttractivePointError.getCode(),
                        customAttractivePointError.getDescription()));
    }

    @ExceptionHandler(CustomAvatarException.class)
    public ResponseEntity<ErrorResult> CustomAvatarExceptionHandler(CustomAvatarException e) {
        log.error("[CustomAvatarException] exception ", e);
        CustomAvatarError customAvatarError = e.getCustomAvatarError();
        return ResponseEntity
                .status(customAvatarError.getStatusCode())
                .body(new ErrorResult(customAvatarError.getCode(), customAvatarError.getDescription()));
    }

    @ExceptionHandler(CustomBlockException.class)
    public ResponseEntity<ErrorResult> CustomBlockExceptionHandler(CustomBlockException e) {
        log.error("[CustomBlockException] exception ", e);
        CustomBlockError customBlockError = e.getCustomBlockError();
        return ResponseEntity
                .status(customBlockError.getStatusCode())
                .body(new ErrorResult(customBlockError.getCode(), customBlockError.getDescription()));
    }

    @ExceptionHandler(CustomCategoryException.class)
    public ResponseEntity<ErrorResult> CustomCategoryExceptionHandler(CustomCategoryException e) {
        log.error("[CustomCategoryException] exception ", e);
        CustomCategoryError customCategoryError = e.getCustomCategoryError();
        return ResponseEntity
                .status(customCategoryError.getStatusCode())
                .body(new ErrorResult(customCategoryError.getCode(), customCategoryError.getDescription()));
    }

    @ExceptionHandler(CustomFeedException.class)
    public ResponseEntity<ErrorResult> CustomFeedExceptionHandler(CustomFeedException e) {
        log.error("[CustomFeedException] exception ", e);
        CustomFeedError customFeedError = e.getCustomFeedError();
        return ResponseEntity
                .status(customFeedError.getStatusCode())
                .body(new ErrorResult(customFeedError.getCode(), customFeedError.getDescription()));
    }

    @ExceptionHandler(CustomKeywordException.class)
    public ResponseEntity<ErrorResult> CustomKeywordExceptionHandler(CustomKeywordException e) {
        log.error("[CustomKeywordException] exception", e);
        CustomKeywordError customKeywordError = e.getCustomKeywordError();
        return ResponseEntity
                .status(customKeywordError.getStatusCode())
                .body(new ErrorResult(customKeywordError.getCode(), customKeywordError.getDescription()));
    }

    @ExceptionHandler(CustomNoticeException.class)
    public ResponseEntity<ErrorResult> CustomNoticeExceptionHandler(CustomNoticeException e) {
        log.error("[CustomNoticeException] exception ", e);
        CustomNoticeError customNoticeError = e.getCustomNoticeError();
        return ResponseEntity
                .status(customNoticeError.getStatusCode())
                .body(new ErrorResult(customNoticeError.getCode(), customNoticeError.getDescription()));
    }

    @ExceptionHandler(CustomNovelException.class)
    public ResponseEntity<ErrorResult> CustomNovelExceptionHandler(CustomNovelException e) {
        log.error("[CustomNovelException] exception ", e);
        CustomNovelError customNovelError = e.getCustomNovelError();
        return ResponseEntity
                .status(customNovelError.getStatusCode())
                .body(new ErrorResult(customNovelError.getCode(), customNovelError.getDescription()));
    }

    @ExceptionHandler(CustomNovelStatisticsException.class)
    public ResponseEntity<ErrorResult> CustomNovelStatisticsExceptionHandler(CustomNovelStatisticsException e) {
        log.error("[CustomNovelStatisticsException] exception", e);
        CustomNovelStatisticsError customNovelStatisticsError = e.getCustomNovelStatisticsError();
        return ResponseEntity
                .status(customNovelStatisticsError.getStatusCode())
                .body(new ErrorResult(customNovelStatisticsError.getCode(),
                        customNovelStatisticsError.getDescription()));
    }

    @ExceptionHandler(CustomUserException.class)
    public ResponseEntity<ErrorResult> CustomUserExceptionHandler(CustomUserException e) {
        log.error("[CustomUserException] exception ", e);
        CustomUserError customUserError = e.getCustomUserError();
        return ResponseEntity
                .status(customUserError.getStatusCode())
                .body(new ErrorResult(customUserError.getCode(), customUserError.getDescription()));
    }

    @ExceptionHandler(CustomUserNovelException.class)
    public ResponseEntity<ErrorResult> CustomUserNovelExceptionHandler(CustomUserNovelException e) {
        log.error("[CustomUserNovelException] exception ", e);
        CustomUserNovelError customUserNovelError = e.getCustomUserNovelError();
        return ResponseEntity
                .status(customUserNovelError.getStatusCode())
                .body(new ErrorResult(customUserNovelError.getCode(), customUserNovelError.getDescription()));
    }

    @ExceptionHandler(CustomUserStatisticsException.class)
    public ResponseEntity<ErrorResult> CustomUserStatisticsExceptionHandler(CustomUserStatisticsException e) {
        log.error("[CustomUserStatisticsException] exception ", e);
        CustomUserStatisticsError customUserStatisticsError = e.getCustomUserStatisticsError();
        return ResponseEntity
                .status(customUserStatisticsError.getStatusCode())
                .body(new ErrorResult(customUserStatisticsError.getCode(), customUserStatisticsError.getDescription()));
    }
}
