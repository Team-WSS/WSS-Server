package org.websoso.WSSServer.feed.report.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.common.exception.ICustomError;

@Getter
@AllArgsConstructor
public enum CustomReportError implements ICustomError {

    SELF_FEED_REPORT_NOT_ALLOWED("REPORT-001", "자신의 피드를 신고할 수 없습니다.", BAD_REQUEST),
    ALREADY_REPORTED_FEED("REPORT-002", "이미 사용자가 신고한 피드입니다.", CONFLICT),
    SELF_COMMENT_REPORT_NOT_ALLOWED("REPORT-003", "자신의 댓글을 신고할 수 없습니다.", BAD_REQUEST),
    ALREADY_REPORTED_COMMENT("REPORT-004", "이미 사용자가 신고한 댓글입니다.", CONFLICT);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;

}
