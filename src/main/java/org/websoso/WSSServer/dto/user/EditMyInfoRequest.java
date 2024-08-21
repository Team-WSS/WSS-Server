package org.websoso.WSSServer.dto.user;

public record EditMyInfoRequest(
        String gender,
        Integer birth
) {
}
