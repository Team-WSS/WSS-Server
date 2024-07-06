package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.ICustomError;

@Getter
@AllArgsConstructor
public enum CustomFeedError implements ICustomError {

    FEED_NOT_FOUND("FEED-001", "해당 ID를 가진 피드를 찾을 수 없습니다.", NOT_FOUND),
    ALREADY_LIKED("FEED-002", "이미 해당 피드에 좋아요를 눌렀습니다.", CONFLICT),
    LIKE_USER_NOT_FOUND("FEED-003", "해당 사용자가 이 피드에 좋아요를 누르지 않았습니다.", NOT_FOUND),
    INVALID_LIKE_COUNT("FEED-004", "좋아요 수가 유효하지 않습니다.", BAD_REQUEST),
    HIDDEN_FEED_ACCESS("FEED-005", "이 피드는 숨겨져 있어 접근할 수 없습니다.", FORBIDDEN),
    BLOCKED_USER_ACCESS("FEED-006", "해당 사용자와 피드 작성자가 차단 상태이므로 이 피드에 접근할 수 없습니다.", FORBIDDEN);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;

}
