package org.websoso.WSSServer.dto.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.websoso.WSSServer.validation.FeedIdConstraint;
import org.websoso.WSSServer.validation.ZeroAllowedUserIdConstraint;

public record NotificationCreateRequest(

        @NotBlank(message = "알림 제목은 비어 있거나, 공백일 수 없습니다.")
        @Size(max = 200, message = "알림 제목은 200자를 초과할 수 없습니다.")
        String notificationTitle,

        @NotBlank(message = "알림 개요는 비어 있거나, 공백일 수 없습니다.")
        @Size(max = 200, message = "알림 개요는 200자를 초과할 수 없습니다.")
        String notificationBody,

        @NotBlank(message = "알림 내용은 비어 있거나, 공백일 수 없습니다.")
        @Size(max = 2000, message = "알림 내용은 2000자를 초과할 수 없습니다.")
        String notificationDetail,

        @NotNull(message = "유저 ID는 필수입니다.")
        @ZeroAllowedUserIdConstraint
        Long userId,

        @FeedIdConstraint
        Long feedId,

        @NotBlank(message = "알림 타입 이름은 필수입니다.")
        String notificationTypeName
) {
}
