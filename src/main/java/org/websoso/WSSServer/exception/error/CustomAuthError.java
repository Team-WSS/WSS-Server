package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.ICustomError;

@Getter
@AllArgsConstructor
public enum CustomAuthError implements ICustomError {

    INVALID_TOKEN("AUTH-001", "유효하지 않은 토큰입니다.", UNAUTHORIZED);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}

