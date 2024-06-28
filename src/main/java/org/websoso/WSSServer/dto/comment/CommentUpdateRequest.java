package org.websoso.WSSServer.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
        @NotBlank(message = "댓글 내용은 비어 있거나, 공백일 수 없습니다.")
        @Size(max = 100, message = "댓글 내용은 100자를 초과할 수 없습니다.")
        String commentContent
) {
}
