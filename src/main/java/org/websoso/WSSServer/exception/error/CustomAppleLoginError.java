package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.ICustomError;

@AllArgsConstructor
@Getter
public enum CustomAppleLoginError implements ICustomError {

    TOKEN_REQUEST_FAILED("APPLE-001", "Apple 서버로부터 토큰을 받아오지 못했습니다.", INTERNAL_SERVER_ERROR),
    CLIENT_SECRET_CREATION_FAILED("APPLE-002", "클라이언트 시크릿을 생성하는 데 실패했습니다.", INTERNAL_SERVER_ERROR),
    PRIVATE_KEY_READ_FAILED("APPLE-003", "프라이빗 키를 읽는 데 실패했습니다.", INTERNAL_SERVER_ERROR),
    INVALID_APPLE_TOKEN_FORMAT("APPLE-004", "idToken 값이 jwt 형식인지 확인이 필요합니다.", BAD_REQUEST),
    HEADER_PARSING_FAILED("APPLE-005", "디코드된 헤더를 Map 형태로 변환할 수 없습니다.", INTERNAL_SERVER_ERROR),
    UNSUPPORTED_JWT_TYPE("APPLE-006", "지원되지 않는 jwt 타입입니다.", BAD_REQUEST),
    EMPTY_JWT("APPLE-007", "비어있는 jwt입니다.", BAD_REQUEST),
    JWT_VERIFICATION_FAILED("APPLE-008", "jwt 검증 또는 분석에 실패했습니다.", INTERNAL_SERVER_ERROR),
    INVALID_APPLE_KEY("APPLE-009", "잘못된 애플 키입니다.", INTERNAL_SERVER_ERROR),
    USER_APPLE_REFRESH_TOKEN_NOT_FOUND("APPLE-010", "유저의 애플 리프레시 토큰을 찾을 수 없습니다.", NOT_FOUND);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
