package org.websoso.WSSServer.dto.block;

import org.websoso.support.logging.masking.MaskingPolicy;
import org.websoso.support.logging.masking.SensitiveData;
public record BlockGetResponse(
        Long blockId,
        Long userId,
        @SensitiveData(MaskingPolicy.NAME)
        String nickname,
        String avatarImage
) {
}
