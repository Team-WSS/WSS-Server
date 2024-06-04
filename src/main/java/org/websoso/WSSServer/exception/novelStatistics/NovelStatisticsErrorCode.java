package org.websoso.WSSServer.exception.novelStatistics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.IErrorCode;

@Getter
@AllArgsConstructor
public enum NovelStatisticsErrorCode implements IErrorCode {

    NOVEL_STATISTICS_NOT_FOUND("NOVEL_STATISTICS-001", "해당 작품의 통계를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
