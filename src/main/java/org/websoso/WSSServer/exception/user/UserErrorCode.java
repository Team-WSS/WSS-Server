package org.websoso.WSSServer.exception.user;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.IErrorCode;

@AllArgsConstructor
@Getter
public enum UserErrorCode implements IErrorCode {

    INVALID_NICKNAME_NULL("USER-001", "닉네임은 null일 수 없습니다.", BAD_REQUEST),
    INVALID_NICKNAME_BLANK("USER-002", "닉네임은 빈칸일 수 없습니다.", BAD_REQUEST),
    INVALID_NICKNAME_START_OR_END_WITH_BLANK("USER-003", "닉네임은 공백으로 시작하거나 끝날 수 없습니다.", BAD_REQUEST),
    INVALID_NICKNAME_LENGTH("USER-004", "닉네임의 길이가 2 ~ 10자가 아닙니다.", BAD_REQUEST),
    INVALID_NICKNAME_PATTERN("USER-005", "닉네임은 한글, 영문, 숫자, 특수문자(-, _)로만 이루어져아 합니다.", BAD_REQUEST),
    USER_NOT_FOUND("USER-006", "해당 ID를 가진 사용자를 찾을 수 없습니다.", NOT_FOUND),
    INVALID_AUTHORIZED("USER-007", "사용자에게 권한이 없습니다.", FORBIDDEN),
    INVALID_USER_ID("USER-008", "유효하지 않은 ID입니다.", BAD_REQUEST),
    DUPLICATED_NICKNAME("USER-009", "중복된 닉네임입니다.", CONFLICT),
    INVALID_PROFILE_STATUS("USER-010", "프로필 상태는 이미 설정된 값과 동일합니다.", BAD_REQUEST);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;

}
