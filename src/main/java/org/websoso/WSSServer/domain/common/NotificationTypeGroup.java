package org.websoso.WSSServer.domain.common;

import java.util.Set;
import lombok.Getter;

@Getter
public enum NotificationTypeGroup {

    NOTICE("공지사항", "이벤트"),
    FEED("지금뜨는글", "댓글", "좋아요"),
    NOVEL("완결 알림", "휴재 복귀 알림");

    private final Set<String> types;

    NotificationTypeGroup(String... types) {
        this.types = Set.copyOf(Set.of(types));
    }

    public static boolean isTypeInGroup(String typeName, NotificationTypeGroup group) {
        return group.types.contains(typeName);
    }
}
