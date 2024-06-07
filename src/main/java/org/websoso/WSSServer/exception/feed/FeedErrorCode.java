package org.websoso.WSSServer.exception.feed;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.IErrorCode;

@Getter
@AllArgsConstructor
public enum FeedErrorCode implements IErrorCode {

    FEED_NOT_FOUND("FEED-001", "해당 ID를 가진 피드를 찾을 수 없습니다.", NOT_FOUND),
    ALREADY_LIKED("FEED-002", "이미 해당 피드에 좋아요를 눌렀습니다.", CONFLICT),
    LIKE_USER_NOT_FOUND("FEED-003", "해당 사용자가 이 피드에 좋아요를 누르지 않았습니다.", NOT_FOUND),
    INVALID_LIKE_COUNT("FEED-004", "좋아요 수가 유효하지 않습니다.", BAD_REQUEST);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;

}
