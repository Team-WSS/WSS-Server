package org.websoso.WSSServer.notification.controller;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 작품 알림 설정 API의 요청 검증 실패 시 클라이언트에 내려가는 에러 메시지를 모아둔다. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NovelNotificationValidationMessage {

    public static final String NOTIFICATION_TYPE_NOT_NULL = "작품 알림 유형은 null일 수 없습니다.";
    public static final String NOVEL_IDS_NOT_EMPTY = "삭제할 작품 ID는 한 개 이상이어야 합니다.";
    public static final String NOVEL_IDS_MAX_SIZE = "한 번에 삭제할 작품은 100개를 초과할 수 없습니다.";
    public static final String NOVEL_ID_NOT_NULL = "작품 ID는 null일 수 없습니다.";
    public static final String NOVEL_ID_POSITIVE = "작품 ID는 양수여야 합니다.";
    public static final String COMPLETION_NOTIFICATION_NOT_NULL = "완결 알림 설정 값은 null일 수 없습니다.";
    public static final String HIATUS_RETURN_NOTIFICATION_NOT_NULL = "휴재 복귀 알림 설정 값은 null일 수 없습니다.";
    public static final String LAST_SUBSCRIPTION_ID_POSITIVE_OR_ZERO = "마지막 구독 ID는 0 이상이어야 합니다.";
    public static final String SIZE_MIN = "조회 개수는 1개 이상이어야 합니다.";
    public static final String SIZE_MAX = "조회 개수는 50개를 초과할 수 없습니다.";
}
