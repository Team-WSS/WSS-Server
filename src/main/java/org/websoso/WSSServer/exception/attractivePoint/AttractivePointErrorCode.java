package org.websoso.WSSServer.exception.attractivePoint;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.IErrorCode;

@Getter
@AllArgsConstructor
public enum AttractivePointErrorCode implements IErrorCode {

    INVALID_ATTRACTIVE_POINT("ATTRACTIVE_POINT-001", "해당 매력포인트는 존재하지 않습니다.", BAD_REQUEST);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
