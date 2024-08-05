package org.websoso.WSSServer.dto.user;

import jakarta.validation.constraints.Size;
import org.websoso.WSSServer.validation.NullAllowedNicknameConstraint;

public record UpdateMyProfileRequest(
        Byte avatarId,

        @NullAllowedNicknameConstraint
        String nickname,

        @Size(max = 60, message = "소개글은 60자를 초과할 수 없습니다.")
        String intro
) {
}
