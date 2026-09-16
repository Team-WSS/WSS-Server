package org.websoso.WSSServer.domain.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 완결·휴재 복귀 알림의 작품 알림 그룹 분류를 검증한다. */
class NotificationTypeGroupTest {

    /** 두 작품 알림 이름이 NOVEL 그룹에 포함되는지 검증한다. */
    @Test
    @DisplayName("완결과 휴재 복귀 알림은 작품 알림으로 분류한다")
    void classifiesNovelNotificationTypes() {
        assertThat(NotificationTypeGroup.isTypeInGroup("완결 알림", NotificationTypeGroup.NOVEL)).isTrue();
        assertThat(NotificationTypeGroup.isTypeInGroup("휴재 복귀 알림", NotificationTypeGroup.NOVEL)).isTrue();
    }
}
