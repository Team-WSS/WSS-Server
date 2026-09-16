package org.websoso.WSSServer.dto.user;

import jakarta.validation.constraints.NotNull;
import org.websoso.WSSServer.validation.BirthConstraint;
import org.websoso.WSSServer.validation.GenderConstraint;
import org.websoso.support.logging.masking.MaskingPolicy;
import org.websoso.support.logging.masking.SensitiveData;

public record EditMyInfoRequest(
        @NotNull
        @GenderConstraint
        @SensitiveData(MaskingPolicy.FULL)
        String gender,

        @NotNull(message = "출생연도는 null일 수 없습니다.")
        @BirthConstraint
        @SensitiveData(MaskingPolicy.FULL)
        Integer birth
) {
}
