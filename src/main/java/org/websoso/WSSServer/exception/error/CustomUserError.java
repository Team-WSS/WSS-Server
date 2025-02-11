package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.ICustomError;

@AllArgsConstructor
@Getter
public enum CustomUserError implements ICustomError {

    INVALID_NICKNAME_NULL("USER-001", "닉네임은 null일 수 없습니다.", BAD_REQUEST),
    INVALID_NICKNAME_BLANK("USER-002", "닉네임은 빈칸일 수 없습니다.", BAD_REQUEST),
    INVALID_NICKNAME_CONTAINS_WHITESPACE("USER-003", "닉네임에 공백이 포함될 수 없습니다.", BAD_REQUEST),
    INVALID_NICKNAME_LENGTH("USER-004", "닉네임의 길이가 2 ~ 10자가 아닙니다.", BAD_REQUEST),
    INVALID_NICKNAME_PATTERN("USER-005", "닉네임은 완성된 한글, 영문, 숫자로만 이루어져야 합니다.", BAD_REQUEST),
    USER_NOT_FOUND("USER-006", "해당 ID를 가진 사용자를 찾을 수 없습니다.", NOT_FOUND),
    INVALID_AUTHORIZED("USER-007", "사용자에게 권한이 없습니다.", FORBIDDEN),
    INVALID_USER_ID("USER-008", "유효하지 않은 ID입니다.", BAD_REQUEST),
    DUPLICATED_NICKNAME("USER-009", "중복된 닉네임입니다.", CONFLICT),
    ALREADY_SET_PROFILE_STATUS("USER-010", "프로필 상태는 이미 설정된 값과 동일합니다.", BAD_REQUEST),
    INVALID_GENDER("USER-011", "성별은 M 또는 F여야 합니다.", BAD_REQUEST),
    INVALID_GENDER_NULL("USER-012", "성별은 null일 수 없습니다.", BAD_REQUEST),
    INVALID_GENDER_BLANK("USER-013", "성별은 빈칸일 수 없습니다.", BAD_REQUEST),
    ALREADY_SET_NICKNAME("USER-014", "닉네임은 이미 설정된 닉네임과 동일합니다.", BAD_REQUEST),
    PRIVATE_PROFILE_STATUS("USER-015", "프로필 공개 설정이 비공개이므로 접근할 수 없습니다.", FORBIDDEN),
    ALREADY_SET_AVATAR("USER-016", "아바타는 이미 설정된 아바타와 동일합니다.", BAD_REQUEST),
    ALREADY_SET_INTRO("USER-017", "소개글은 이미 설정된 소개글과 동일합니다.", BAD_REQUEST),
    INACCESSIBLE_USER_PROFILE("USER-018", "해당 사용자는 접근할 수 없는 상태입니다.", FORBIDDEN),
    TERMS_AGREEMENT_REQUIRED("USER-019", "서비스 이용약관 및 개인정보 수집 동의는 필수입니다.", BAD_REQUEST);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;

}
