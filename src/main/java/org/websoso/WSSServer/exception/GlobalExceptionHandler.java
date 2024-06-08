package org.websoso.WSSServer.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.websoso.WSSServer.exception.block.BlockErrorCode;
import org.websoso.WSSServer.exception.block.exception.AlreadyBlockedException;
import org.websoso.WSSServer.exception.category.CategoryErrorCode;
import org.websoso.WSSServer.exception.category.exception.InvalidCategoryException;
import org.websoso.WSSServer.exception.common.ErrorResult;
import org.websoso.WSSServer.exception.feed.FeedErrorCode;
import org.websoso.WSSServer.exception.feed.exception.InvalidFeedException;
import org.websoso.WSSServer.exception.notice.NoticeErrorCode;
import org.websoso.WSSServer.exception.notice.exception.ForbiddenNoticeManipulationException;
import org.websoso.WSSServer.exception.notice.exception.NoticeNotFoundException;
import org.websoso.WSSServer.exception.novel.NovelErrorCode;
import org.websoso.WSSServer.exception.novel.exception.InvalidNovelException;
import org.websoso.WSSServer.exception.novelStatistics.NovelStatisticsErrorCode;
import org.websoso.WSSServer.exception.novelStatistics.exception.InvalidNovelStatisticsException;
import org.websoso.WSSServer.exception.user.UserErrorCode;
import org.websoso.WSSServer.exception.user.exception.InvalidAuthorizedException;
import org.websoso.WSSServer.exception.user.exception.InvalidNicknameException;
import org.websoso.WSSServer.exception.user.exception.InvalidUserException;

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

    @ExceptionHandler(InvalidCategoryException.class)
    public ResponseEntity<ErrorResult> InvalidCategoryExceptionHandler(InvalidCategoryException e) {
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResult> InvalidRequestBodyExceptionHandler(MethodArgumentNotValidException e) {
        log.error("[MethodArgumentNotValidException] exception ", e);
        HttpStatus httpStatus = HttpStatus.valueOf(e.getStatusCode().value());
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResult(httpStatus.name(),
                        e.getBindingResult().getAllErrors().get(0).getDefaultMessage()));
    }

    @ExceptionHandler(InvalidFeedException.class)
    public ResponseEntity<ErrorResult> InvalidFeedExceptionHandler(InvalidFeedException e) {
        log.error("[InvalidFeedException] exception ", e);
        FeedErrorCode feedErrorCode = e.getFeedErrorCode();
        return ResponseEntity
                .status(feedErrorCode.getStatusCode())
                .body(new ErrorResult(feedErrorCode.getCode(), feedErrorCode.getDescription()));
    }

    @ExceptionHandler(ForbiddenNoticeManipulationException.class)
    public ResponseEntity<ErrorResult> ForbiddenNoticeManipulationExceptionHandler(
            ForbiddenNoticeManipulationException e) {
        log.error("[ForbiddenNoticeManipulationException] exception ", e);
        NoticeErrorCode noticeErrorCode = e.getNoticeErrorCode();
        return ResponseEntity
                .status(noticeErrorCode.getStatusCode())
                .body(new ErrorResult(noticeErrorCode.getCode(), noticeErrorCode.getDescription()));
    }

    @ExceptionHandler(NoticeNotFoundException.class)
    public ResponseEntity<ErrorResult> NoticeNotFoundExceptionHandler(NoticeNotFoundException e) {
        log.error("[NoticeNotFoundException] exception ", e);
        NoticeErrorCode noticeErrorCode = e.getNoticeErrorCode();
        return ResponseEntity
                .status(noticeErrorCode.getStatusCode())
                .body(new ErrorResult(noticeErrorCode.getCode(), noticeErrorCode.getDescription()));
    }

    @ExceptionHandler(InvalidNovelException.class)
    public ResponseEntity<ErrorResult> InvalidNovelExceptionHandler(InvalidNovelException e) {
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

    @ExceptionHandler(AlreadyBlockedException.class)
    public ResponseEntity<ErrorResult> AlreadyBlockedExceptionHandler(AlreadyBlockedException e) {
        log.error("[AlreadyBlockedException] exception ", e);
        BlockErrorCode blockErrorCode = e.getBlockErrorCode();
        return ResponseEntity
                .status(blockErrorCode.getStatusCode())
                .body(new ErrorResult(blockErrorCode.getCode(), blockErrorCode.getDescription()));
    }
}
