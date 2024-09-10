package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.ICustomError;

@Getter
@AllArgsConstructor
public enum CustomAuthError implements ICustomError {

    EXPIRED_REFRESH_TOKEN("AUTH-001", "만료된 리프레시 토큰입니다.", UNAUTHORIZED),
    INVALID_TOKEN("AUTH-002", "유효하지 않은 토큰입니다.", UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("AUTH-003", "등록되지 않은 리프레시 토큰입니다.", UNAUTHORIZED);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}

