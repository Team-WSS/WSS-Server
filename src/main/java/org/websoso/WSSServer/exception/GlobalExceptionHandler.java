package org.websoso.WSSServer.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.common.ErrorResult;
<<<<<<< HEAD
import org.websoso.WSSServer.exception.common.ICustomError;
=======
import org.websoso.WSSServer.exception.feed.FeedErrorCode;
import org.websoso.WSSServer.exception.feed.exception.CustomFeedException;
import org.websoso.WSSServer.exception.keyword.KeywordErrorCode;
import org.websoso.WSSServer.exception.keyword.exception.CustomKeywordException;
import org.websoso.WSSServer.exception.notice.NoticeErrorCode;
import org.websoso.WSSServer.exception.notice.exception.CustomNoticeException;
import org.websoso.WSSServer.exception.novel.NovelErrorCode;
import org.websoso.WSSServer.exception.novel.exception.CustomNovelException;
import org.websoso.WSSServer.exception.novelStatistics.NovelStatisticsErrorCode;
import org.websoso.WSSServer.exception.novelStatistics.exception.InvalidNovelStatisticsException;
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
>>>>>>> c00f879 ([MERGE] git rebase dev)

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

<<<<<<< HEAD
=======
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

    @ExceptionHandler(InvalidNovelStatisticsException.class)
    public ResponseEntity<ErrorResult> InvalidNovelStatisticsExceptionHandler(InvalidNovelStatisticsException e) {
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

>>>>>>> c00f879 ([MERGE] git rebase dev)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResult> HttpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e) {
        log.error("[HttpMessageNotReadableException] exception ", e);
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new ErrorResult(BAD_REQUEST.name(), "잘못된 JSON 형식입니다."));
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
