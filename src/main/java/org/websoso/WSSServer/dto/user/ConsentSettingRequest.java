package org.websoso.WSSServer.dto.user;

import jakarta.validation.constraints.NotNull;

public record ConsentSettingRequest(
        @NotNull(message = "웹소소 이용약관 동의 여부는 null일 수 없습니다.")
        Boolean serviceAgreed,
        @NotNull(message = "개인정보 수집 동의 여부는 null일 수 없습니다.")
        Boolean privacyAgreed,
        @NotNull(message = "마케팅 활용 및 이벤트 정보 수신 동의 여부는 null일 수 없습니다.")
        Boolean marketingAgreed
) {
}
