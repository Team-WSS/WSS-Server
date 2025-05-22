package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.ICustomError;

@Getter
@AllArgsConstructor
public enum CustomFilteringError implements ICustomError {

    SORT_CRITERIA_NOT_FOUND("Filtering-001", "해당 정렬기준을 찾을 수 없습니다.", NOT_FOUND),
    FEED_GET_OPTION_NOT_FOUND("Filtering-002", "해당 소소 피드 조회 조건을 찾을 수 없습니다.", NOT_FOUND);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
