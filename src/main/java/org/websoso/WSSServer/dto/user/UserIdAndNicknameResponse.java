package org.websoso.WSSServer.dto.user;

import org.websoso.WSSServer.user.domain.User;
import org.websoso.support.logging.masking.MaskingPolicy;
import org.websoso.support.logging.masking.SensitiveData;

public record UserIdAndNicknameResponse(
        Long userId,
        @SensitiveData(MaskingPolicy.NAME)
        String nickname,
        @SensitiveData(MaskingPolicy.FULL)
        String gender
) {

    public static UserIdAndNicknameResponse of(User user) {
        return new UserIdAndNicknameResponse(
                user.getUserId(),
                user.getNickname(),
                user.getGender().name()
        );
    }
}
