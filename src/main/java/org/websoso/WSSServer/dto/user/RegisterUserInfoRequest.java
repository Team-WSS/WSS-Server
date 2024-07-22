package org.websoso.WSSServer.dto.user;

import java.time.Year;

public record RegisterUserInfoRequest(
        String nickname,
        String gender,
        Year birth
) {
}
