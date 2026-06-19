package org.websoso.WSSServer.user.event;

public record WithdrawUserEvent(
        Long userId
) {
    public static WithdrawUserEvent of(Long userId) {
        return new WithdrawUserEvent(userId);
    }
}
