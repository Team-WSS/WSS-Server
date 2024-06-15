package org.websoso.WSSServer.exception.avatar;


import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.IErrorCode;

@AllArgsConstructor
@Getter
public enum AvatarErrorCode implements IErrorCode {

    AVATAR_NOT_FOUND("AVATAR-001", "해당 ID를 가진 아바타를 찾을 수 없습니다.", NOT_FOUND);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}

