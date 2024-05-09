package org.websoso.WSSServer.exception.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.common.IErrorCode;

@AllArgsConstructor
@Getter
public enum UserErrorCode implements IErrorCode {

    INVALID_NICKNAME_NULL("USER-001", "닉네임은 null일 수 없습니다."),
    INVALID_NICKNAME_BLANK("USER-002", "닉네임은 빈칸일 수 없습니다."),
    INVALID_NICKNAME_START_OR_END_WITH_BLANK("USER-003", "닉네임은 공백으로 시작하거나 끝날 수 없습니다."),
    INVALID_NICKNAME_LENGTH("USER-004", "닉네임의 길이가 2 ~ 10자가 아닙니다."),
    INVALID_NICKNAME_PATTERN("USER-005", "닉네임은 한글, 영문, 숫자로만 이루어져아 합니다.");

    private final String code;
    private final String description;
}
