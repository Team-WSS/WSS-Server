package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.common.exception.ICustomError;

@AllArgsConstructor
@Getter
public enum CustomNotificationError implements ICustomError {

    NOTIFICATION_NOT_FOUND("NOTIFICATION-001", "해당 ID를 가진 알림을 찾을 수 없습니다.", NOT_FOUND),
    NOTIFICATION_READ_FORBIDDEN("NOTIFICATION-002", "해당 알림의 대상 유저가 아닙니다.", FORBIDDEN),
    NOTIFICATION_TYPE_INVALID("NOTIFICATION-003", "해당 알림은 요구된 알림 타입에 적합하지 않습니다.", BAD_REQUEST),
    NOTIFICATION_ALREADY_READ("NOTIFICATION-004", "해당 알림은 유저가 이미 읽은 알림입니다.", CONFLICT),
    NOTIFICATION_ADMIN_ONLY("NOTIFICATION-005", "관리자가 아닌 계정은 알림을 작성 혹은 수정 혹은 삭제할 수 없습니다.", FORBIDDEN),
    NOTIFICATION_NOT_NOTICE_TYPE("NOTIFICATION-006", "해당 알림 타입은 NOTICE에 속하지 않습니다", BAD_REQUEST);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
