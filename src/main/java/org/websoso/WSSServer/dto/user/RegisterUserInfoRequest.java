package org.websoso.WSSServer.dto.user;

import java.time.Year;
import org.websoso.WSSServer.domain.common.Gender;

public record RegisterUserInfoRequest(
        String nickname,
        Gender gender,
        Year birth
) {
}
