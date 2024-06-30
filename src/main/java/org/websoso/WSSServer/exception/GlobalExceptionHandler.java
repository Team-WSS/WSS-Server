package org.websoso.WSSServer.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.websoso.WSSServer.exception.avatar.AvatarErrorCode;
import org.websoso.WSSServer.exception.avatar.exception.CustomAvatarException;
import org.websoso.WSSServer.exception.block.BlockErrorCode;
import org.websoso.WSSServer.exception.block.exception.CustomBlockException;
import org.websoso.WSSServer.exception.category.CategoryErrorCode;
import org.websoso.WSSServer.exception.category.exception.CustomCategoryException;
import org.websoso.WSSServer.exception.common.ErrorResult;
import org.websoso.WSSServer.exception.feed.FeedErrorCode;
import org.websoso.WSSServer.exception.feed.exception.CustomFeedException;
import org.websoso.WSSServer.exception.keyword.KeywordErrorCode;
import org.websoso.WSSServer.exception.keyword.exception.CustomKeywordException;
import org.websoso.WSSServer.exception.notice.NoticeErrorCode;
import org.websoso.WSSServer.exception.notice.exception.CustomNoticeException;
import org.websoso.WSSServer.exception.novel.NovelErrorCode;
import org.websoso.WSSServer.exception.novel.exception.CustomNovelException;
import org.websoso.WSSServer.exception.novelStatistics.NovelStatisticsErrorCode;
import org.websoso.WSSServer.exception.novelStatistics.exception.CustomNovelStatisticsException;
import org.websoso.WSSServer.exception.user.UserErrorCode;
import org.websoso.WSSServer.exception.user.exception.CustomUserException;
import org.websoso.WSSServer.exception.user.exception.InvalidAuthorizedException;
import org.websoso.WSSServer.exception.user.exception.InvalidNicknameException;
import org.websoso.WSSServer.exception.user.exception.InvalidUserException;
import org.websoso.WSSServer.exception.user.exception.InvalidUserIdException;
import org.websoso.WSSServer.exception.user.exception.UserNotFoundException;
import org.websoso.WSSServer.exception.userNovel.UserNovelErrorCode;
import org.websoso.WSSServer.exception.userNovel.exception.InvalidReadStatusException;
import org.websoso.WSSServer.exception.userNovel.exception.NovelAlreadyRegisteredException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidNicknameException.class)
    public ResponseEntity<ErrorResult> InvalidNicknameExceptionHandler(InvalidNicknameException e) {
        log.error("[InvalidNicknameException] exception ", e);
        UserErrorCode userErrorCode = e.getUserErrorCode();
        return ResponseEntity
                .status(userErrorCode.getStatusCode())
                .body(new ErrorResult(userErrorCode.getCode(), userErrorCode.getDescription()));
    }

    @ExceptionHandler(InvalidUserException.class)
    public ResponseEntity<ErrorResult> InvalidUserExceptionHandler(InvalidUserException e) {
        log.error("[InvalidUserException] exception ", e);
        UserErrorCode userErrorCode = e.getUserErrorCode();
        return ResponseEntity
                .status(userErrorCode.getStatusCode())
                .body(new ErrorResult(userErrorCode.getCode(), userErrorCode.getDescription()));
    }

    @ExceptionHandler(CustomCategoryException.class)
    public ResponseEntity<ErrorResult> InvalidCategoryExceptionHandler(CustomCategoryException e) {
        log.error("[InvalidCategoryException] exception ", e);
        CategoryErrorCode categoryErrorCode = e.getCategoryErrorCode();
        return ResponseEntity
                .status(categoryErrorCode.getStatusCode())
                .body(new ErrorResult(categoryErrorCode.getCode(), categoryErrorCode.getDescription()));
    }

    @ExceptionHandler(InvalidAuthorizedException.class)
    public ResponseEntity<ErrorResult> InvalidUserAuthorizedExceptionHandler(InvalidAuthorizedException e) {
        log.error("[InvalidAuthorizedException] exception ", e);
        UserErrorCode userErrorCode = e.getUserErrorCode();
        return ResponseEntity
                .status(userErrorCode.getStatusCode())
                .body(new ErrorResult(userErrorCode.getCode(), userErrorCode.getDescription()));
    }

    @ExceptionHandler(CustomBlockException.class)
    public ResponseEntity<ErrorResult> AvatarNotFoundExceptionHandler(CustomBlockException e) {
        log.error("[AvatarNotFoundException] exception ", e);
        BlockErrorCode blockErrorCode = e.getBlockErrorCode();
        return ResponseEntity
                .status(blockErrorCode.getStatusCode())
                .body(new ErrorResult(blockErrorCode.getCode(), blockErrorCode.getDescription()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResult> InvalidRequestBodyExceptionHandler(MethodArgumentNotValidException e) {
        log.error("[MethodArgumentNotValidException] exception ", e);
        HttpStatus httpStatus = HttpStatus.valueOf(e.getStatusCode().value());
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResult(httpStatus.name(),
                        e.getBindingResult().getAllErrors().get(0).getDefaultMessage()));
    }

    @ExceptionHandler(CustomFeedException.class)
    public ResponseEntity<ErrorResult> InvalidFeedExceptionHandler(CustomFeedException e) {
        log.error("[InvalidFeedException] exception ", e);
        FeedErrorCode feedErrorCode = e.getFeedErrorCode();
        return ResponseEntity
                .status(feedErrorCode.getStatusCode())
                .body(new ErrorResult(feedErrorCode.getCode(), feedErrorCode.getDescription()));
    }

    @ExceptionHandler(CustomNoticeException.class)
    public ResponseEntity<ErrorResult> NoticeNotFoundExceptionHandler(CustomNoticeException e) {
        log.error("[CustomNoticeException] exception ", e);
        NoticeErrorCode noticeErrorCode = e.getNoticeErrorCode();
        return ResponseEntity
                .status(noticeErrorCode.getStatusCode())
                .body(new ErrorResult(noticeErrorCode.getCode(), noticeErrorCode.getDescription()));
    }

    @ExceptionHandler(CustomNovelException.class)
    public ResponseEntity<ErrorResult> InvalidNovelExceptionHandler(CustomNovelException e) {
        log.error("[InvalidNovelException] exception ", e);
        NovelErrorCode novelErrorCode = e.getNovelErrorCode();
        return ResponseEntity
                .status(novelErrorCode.getStatusCode())
                .body(new ErrorResult(novelErrorCode.getCode(), novelErrorCode.getDescription()));
    }

    @ExceptionHandler(CustomNovelStatisticsException.class)
    public ResponseEntity<ErrorResult> InvalidNovelStatisticsExceptionHandler(CustomNovelStatisticsException e) {
        log.error("[InvalidNovelStatisticsException] exception", e);
        NovelStatisticsErrorCode novelStatisticsErrorCode = e.getNovelStatisticsErrorCode();
        return ResponseEntity
                .status(novelStatisticsErrorCode.getStatusCode())
                .body(new ErrorResult(novelStatisticsErrorCode.getCode(), novelStatisticsErrorCode.getDescription()));
    }

    @ExceptionHandler(NovelAlreadyRegisteredException.class)
    public ResponseEntity<ErrorResult> NovelAlreadyRegisteredExceptionHandler(NovelAlreadyRegisteredException e) {
        log.error("[NovelAlreadyRegisteredException] exception", e);
        UserNovelErrorCode userNovelErrorCode = e.getUserNovelErrorCode();
        return ResponseEntity
                .status(userNovelErrorCode.getStatusCode())
                .body(new ErrorResult(userNovelErrorCode.getCode(), userNovelErrorCode.getDescription()));
    }

    @ExceptionHandler(InvalidReadStatusException.class)
    public ResponseEntity<ErrorResult> InvalidReadStatusExceptionHandler(InvalidReadStatusException e) {
        log.error("[InvalidReadStatusException] exception", e);
        UserNovelErrorCode userNovelErrorCode = e.getUserNovelErrorCode();
        return ResponseEntity
                .status(userNovelErrorCode.getStatusCode())
                .body(new ErrorResult(userNovelErrorCode.getCode(), userNovelErrorCode.getDescription()));
    }

    @ExceptionHandler(CustomKeywordException.class)
    public ResponseEntity<ErrorResult> InvalidKeywordExceptionHandler(CustomKeywordException e) {
        log.error("[InvalidKeywordException] exception", e);
        KeywordErrorCode keywordErrorCode = e.getKeywordErrorCode();
        return ResponseEntity
                .status(keywordErrorCode.getStatusCode())
                .body(new ErrorResult(keywordErrorCode.getCode(), keywordErrorCode.getDescription()));
    }

    @ExceptionHandler(CustomAvatarException.class)
    public ResponseEntity<ErrorResult> AvatarNotFoundExceptionHandler(CustomAvatarException e) {
        log.error("[AvatarNotFoundException] exception ", e);
        AvatarErrorCode avatarErrorCode = e.getAvatarErrorCode();
        return ResponseEntity
                .status(avatarErrorCode.getStatusCode())
                .body(new ErrorResult(avatarErrorCode.getCode(), avatarErrorCode.getDescription()));
    }

    @ExceptionHandler(InvalidUserIdException.class)
    public ResponseEntity<ErrorResult> InvalidUserIdExceptionHandler(InvalidUserIdException e) {
        log.error("[InvalidUserIdException] exception ", e);
        UserErrorCode userErrorCode = e.getUserErrorCode();
        return ResponseEntity
                .status(userErrorCode.getStatusCode())
                .body(new ErrorResult((userErrorCode.getCode()), userErrorCode.getDescription()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResult> UserNotFoundExceptionHandler(UserNotFoundException e) {
        log.error("[UserNotFoundException] exception ", e);
        UserErrorCode userErrorCode = e.getUserErrorCode();
        return ResponseEntity
                .status(userErrorCode.getStatusCode())
                .body(new ErrorResult(userErrorCode.getCode(), userErrorCode.getDescription()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResult> HttpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e) {
        log.error("[HttpMessageNotReadableException] exception ", e);
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new ErrorResult(BAD_REQUEST.name(), "잘못된 JSON 형식입니다."));
    }

    @ExceptionHandler(CustomUserException.class)
    public ResponseEntity<ErrorResult> CustomUserExceptionHandler(CustomUserException e) {
        log.error("[CustomUserException] exception ", e);
        UserErrorCode userErrorCode = e.getUserErrorCode();
        return ResponseEntity
                .status(userErrorCode.getStatusCode())
                .body(new ErrorResult(userErrorCode.getCode(), userErrorCode.getDescription()));
    }
}
