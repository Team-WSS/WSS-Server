package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.common.exception.ICustomError;

@AllArgsConstructor
@Getter
public enum CustomAvatarError implements ICustomError {

    AVATAR_NOT_FOUND("AVATAR-001", "해당 ID를 가진 아바타를 찾을 수 없습니다.", NOT_FOUND),
    UNSELECTABLE_AVATAR("AVATAR-002", "선택할 수 없는 아바타입니다.", FORBIDDEN);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;

}
