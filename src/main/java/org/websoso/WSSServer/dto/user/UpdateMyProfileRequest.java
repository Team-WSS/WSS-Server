package org.websoso.WSSServer.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.websoso.WSSServer.validation.NullAllowedNicknameConstraint;

public record UpdateMyProfileRequest(
        Long avatarId,

        @NullAllowedNicknameConstraint
        String nickname,

        @Size(max = 60, message = "소개글은 60자를 초과할 수 없습니다.")
        String intro,

        @NotNull(message = "선호 장르는 null일 수 없습니다.")
        List<String> genrePreferences
) {
    @Deprecated
    public Long getMappedAvatarId() {
        if (this.avatarId == null) {
            return null;
        }

        return switch (this.avatarId.intValue()) {
            case 1 -> 1L;
            case 2 -> 4L;
            case 3 -> 5L;

            default -> null;
        };
    }
}
