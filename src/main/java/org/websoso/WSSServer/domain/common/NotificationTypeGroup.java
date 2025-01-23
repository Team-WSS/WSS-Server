package org.websoso.WSSServer.domain.common;

import java.util.Set;
import lombok.Getter;

@Getter
public enum NotificationTypeGroup {

    NOTICE("공지사항", "이벤트"),
    FEED("지금뜨는수다글", "댓글", "좋아요");

    private final Set<String> types;

    NotificationTypeGroup(String... types) {
        this.types = Set.copyOf(Set.of(types));
    }

    public static boolean isTypeInGroup(String typeName, NotificationTypeGroup group) {
        return group.types.contains(typeName);
    }
}
