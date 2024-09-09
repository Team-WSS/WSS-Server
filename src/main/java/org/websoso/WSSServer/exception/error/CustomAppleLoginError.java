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

    MISSING_AUTHORIZATION_CODE("APPLE-001", "인증 코드를 입력하지 않았습니다.", BAD_REQUEST),
    TOKEN_REQUEST_FAILED("APPLE-003", "Apple 서버로부터 토큰을 받아오지 못했습니다.", INTERNAL_SERVER_ERROR),
    ID_TOKEN_PARSE_FAILED("APPLE-004", "Apple ID 토큰을 파싱하지 못했습니다.", INTERNAL_SERVER_ERROR),
    USER_INFO_RETRIEVAL_FAILED("APPLE-005", "Apple에서 사용자 정보를 가져오지 못했습니다.", NOT_FOUND),
    CLIENT_SECRET_CREATION_FAILED("APPLE-006", "클라이언트 시크릿을 생성하는 데 실패했습니다.", INTERNAL_SERVER_ERROR),
    PRIVATE_KEY_READ_FAILED("APPLE-007", "프라이빗 키를 읽는 데 실패했습니다.", INTERNAL_SERVER_ERROR);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
