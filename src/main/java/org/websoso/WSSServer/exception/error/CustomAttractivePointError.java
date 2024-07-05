package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.ICustomError;

@Getter
@AllArgsConstructor
public enum CustomAttractivePointError implements ICustomError {

    ATTRACTIVE_POINT_NOT_FOUND("ATTRACTIVE_POINT-001", "해당 서재 작품의 매력포인트를 찾을 수 없습니다.", NOT_FOUND),
    INVALID_ATTRACTIVE_POINT("ATTRACTIVE_POINT-002", "해당 매력포인트는 존재하지 않습니다.", BAD_REQUEST);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
