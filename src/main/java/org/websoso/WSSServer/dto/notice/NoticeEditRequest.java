package org.websoso.WSSServer.dto.notice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.websoso.WSSServer.validation.UserIdConstraint;

public record NoticeEditRequest(

        @NotBlank(message = "공지 제목은 비어 있거나, 공백일 수 없습니다.")
        @Size(max = 200, message = "공지 제목은 200자를 초과할 수 없습니다.")
        String noticeTitle,

        @NotBlank(message = "공지 내용은 비어 있거나, 공백일 수 없습니다.")
        @Size(max = 2000, message = "공지 내용은 2000자를 초과할 수 없습니다.")
        String noticeContent,

        @UserIdConstraint
        Long userId
) {
}
