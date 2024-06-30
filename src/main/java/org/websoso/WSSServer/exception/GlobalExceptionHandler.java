package org.websoso.WSSServer.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.websoso.WSSServer.exception.attractivePoint.AttractivePointErrorCode;
import org.websoso.WSSServer.exception.attractivePoint.exception.CustomAttractivePointException;
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
import org.websoso.WSSServer.exception.userNovel.UserNovelErrorCode;
import org.websoso.WSSServer.exception.userNovel.exception.CustomUserNovelException;
import org.websoso.WSSServer.exception.userStatistics.UserStatisticsErrorCode;
import org.websoso.WSSServer.exception.userStatistics.exception.CustomUserStatisticsException;

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
        AttractivePointErrorCode attractivePointErrorCode = e.getAttractivePointErrorCode();
        return ResponseEntity
                .status(attractivePointErrorCode.getStatusCode())
                .body(new ErrorResult(attractivePointErrorCode.getCode(), attractivePointErrorCode.getDescription()));
    }

    @ExceptionHandler(CustomAvatarException.class)
    public ResponseEntity<ErrorResult> CustomAvatarExceptionHandler(CustomAvatarException e) {
        log.error("[CustomAvatarException] exception ", e);
        AvatarErrorCode avatarErrorCode = e.getAvatarErrorCode();
        return ResponseEntity
                .status(avatarErrorCode.getStatusCode())
                .body(new ErrorResult(avatarErrorCode.getCode(), avatarErrorCode.getDescription()));
    }

    @ExceptionHandler(CustomBlockException.class)
    public ResponseEntity<ErrorResult> CustomBlockExceptionHandler(CustomBlockException e) {
        log.error("[CustomBlockException] exception ", e);
        BlockErrorCode blockErrorCode = e.getBlockErrorCode();
        return ResponseEntity
                .status(blockErrorCode.getStatusCode())
                .body(new ErrorResult(blockErrorCode.getCode(), blockErrorCode.getDescription()));
    }

    @ExceptionHandler(CustomCategoryException.class)
    public ResponseEntity<ErrorResult> CustomCategoryExceptionHandler(CustomCategoryException e) {
        log.error("[CustomCategoryException] exception ", e);
        CategoryErrorCode categoryErrorCode = e.getCategoryErrorCode();
        return ResponseEntity
                .status(categoryErrorCode.getStatusCode())
                .body(new ErrorResult(categoryErrorCode.getCode(), categoryErrorCode.getDescription()));
    }

    @ExceptionHandler(CustomFeedException.class)
    public ResponseEntity<ErrorResult> CustomFeedExceptionHandler(CustomFeedException e) {
        log.error("[CustomFeedException] exception ", e);
        FeedErrorCode feedErrorCode = e.getFeedErrorCode();
        return ResponseEntity
                .status(feedErrorCode.getStatusCode())
                .body(new ErrorResult(feedErrorCode.getCode(), feedErrorCode.getDescription()));
    }

    @ExceptionHandler(CustomKeywordException.class)
    public ResponseEntity<ErrorResult> CustomKeywordExceptionHandler(CustomKeywordException e) {
        log.error("[CustomKeywordException] exception", e);
        KeywordErrorCode keywordErrorCode = e.getKeywordErrorCode();
        return ResponseEntity
                .status(keywordErrorCode.getStatusCode())
                .body(new ErrorResult(keywordErrorCode.getCode(), keywordErrorCode.getDescription()));
    }

    @ExceptionHandler(CustomNoticeException.class)
    public ResponseEntity<ErrorResult> CustomNoticeExceptionHandler(CustomNoticeException e) {
        log.error("[CustomNoticeException] exception ", e);
        NoticeErrorCode noticeErrorCode = e.getNoticeErrorCode();
        return ResponseEntity
                .status(noticeErrorCode.getStatusCode())
                .body(new ErrorResult(noticeErrorCode.getCode(), noticeErrorCode.getDescription()));
    }

    @ExceptionHandler(CustomNovelException.class)
    public ResponseEntity<ErrorResult> CustomNovelExceptionHandler(CustomNovelException e) {
        log.error("[CustomNovelException] exception ", e);
        NovelErrorCode novelErrorCode = e.getNovelErrorCode();
        return ResponseEntity
                .status(novelErrorCode.getStatusCode())
                .body(new ErrorResult(novelErrorCode.getCode(), novelErrorCode.getDescription()));
    }

    @ExceptionHandler(CustomNovelStatisticsException.class)
    public ResponseEntity<ErrorResult> CustomNovelStatisticsExceptionHandler(CustomNovelStatisticsException e) {
        log.error("[CustomNovelStatisticsException] exception", e);
        NovelStatisticsErrorCode novelStatisticsErrorCode = e.getNovelStatisticsErrorCode();
        return ResponseEntity
                .status(novelStatisticsErrorCode.getStatusCode())
                .body(new ErrorResult(novelStatisticsErrorCode.getCode(), novelStatisticsErrorCode.getDescription()));
    }

    @ExceptionHandler(CustomUserException.class)
    public ResponseEntity<ErrorResult> CustomUserExceptionHandler(CustomUserException e) {
        log.error("[CustomUserException] exception ", e);
        UserErrorCode userErrorCode = e.getUserErrorCode();
        return ResponseEntity
                .status(userErrorCode.getStatusCode())
                .body(new ErrorResult(userErrorCode.getCode(), userErrorCode.getDescription()));
    }

    @ExceptionHandler(CustomUserNovelException.class)
    public ResponseEntity<ErrorResult> CustomUserNovelExceptionHandler(CustomUserNovelException e) {
        log.error("[CustomUserNovelException] exception ", e);
        UserNovelErrorCode userNovelErrorCode = e.getUserNovelErrorCode();
        return ResponseEntity
                .status(userNovelErrorCode.getStatusCode())
                .body(new ErrorResult(userNovelErrorCode.getCode(), userNovelErrorCode.getDescription()));
    }

    @ExceptionHandler(CustomUserStatisticsException.class)
    public ResponseEntity<ErrorResult> CustomUserStatisticsExceptionHandler(CustomUserStatisticsException e) {
        log.error("[CustomUserStatisticsException] exception ", e);
        UserStatisticsErrorCode userStatisticsErrorCode = e.getUserStatisticsErrorCode();
        return ResponseEntity
                .status(userStatisticsErrorCode.getStatusCode())
                .body(new ErrorResult(userStatisticsErrorCode.getCode(), userStatisticsErrorCode.getDescription()));
    }
}
