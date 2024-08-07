package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.ICustomError;

@Getter
@AllArgsConstructor
public enum CustomCommentError implements ICustomError {

    COMMENT_NOT_FOUND("COMMENT-001", "해당 ID를 가진 댓글을 찾을 수 없습니다.", NOT_FOUND),
    COMMENT_NOT_BELONG_TO_FEED("COMMENT-002", "댓글이 지정된 피드에 속하지 않습니다.", NOT_FOUND);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;

}
