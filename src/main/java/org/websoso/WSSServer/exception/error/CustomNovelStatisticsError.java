package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.IErrorCode;

@Getter
@AllArgsConstructor
public enum CustomNovelStatisticsError implements IErrorCode {

    NOVEL_STATISTICS_NOT_FOUND("NOVEL_STATISTICS-001", "해당 작품의 통계를 찾을 수 없습니다.", NOT_FOUND),
    INVALID_NOVEL_FEED_COUNT("NOVEL_STATISTICS-002", "작품 피드 수가 유효하지 않습니다.", CONFLICT);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;

}
