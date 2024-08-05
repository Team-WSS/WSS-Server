package org.websoso.WSSServer.dto.user;

import jakarta.validation.constraints.Size;
import org.websoso.WSSServer.validation.NicknameConstraint;

public record UpdateMyProfileRequest(
        Byte avatarId,

        @NicknameConstraint
        String nickname,

        @Size(max = 60, message = "intro는 60자를 초과할 수 없습니다.")
        String intro
) {
}
