package org.websoso.WSSServer.dto.AppVersion;

import jakarta.validation.constraints.NotBlank;

public record MinimumVersionUpdateRequest(
        @NotBlank(message = "os는 비어 있거나, 공백일 수 없습니다.")
        String os,
        @NotBlank(message = "최소 지원 버전은 비어 있거나, 공백일 수 없습니다.")
        String minimumVersion
) {
}
