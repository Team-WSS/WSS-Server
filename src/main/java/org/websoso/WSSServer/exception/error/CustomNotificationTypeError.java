package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.common.exception.ICustomError;

@AllArgsConstructor
@Getter
public enum CustomNotificationTypeError implements ICustomError {

    NOTIFICATION_TYPE_NOT_FOUND("NOTIFICATION-TYPE-001", "해당 이름을 가진 알림 타입을 찾을 수 없습니다.", NOT_FOUND);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
