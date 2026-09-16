package org.websoso.WSSServer.dto.user;

import org.websoso.support.logging.masking.MaskingPolicy;
import org.websoso.support.logging.masking.SensitiveData;
public record UserBasicInfo(
        Long userId,
        @SensitiveData(MaskingPolicy.NAME)
        String nickname,
        String avatarImage
) {
    public static UserBasicInfo of(Long userId, String nickname, String avatarImage) {
        return new UserBasicInfo(
                userId,
                nickname,
                avatarImage
        );
    }
}
