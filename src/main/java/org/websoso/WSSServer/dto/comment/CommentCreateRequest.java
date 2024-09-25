package org.websoso.WSSServer.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotBlank(message = "댓글 내용은 null 이거나, 공백일 수 없습니다.")
        @Size(max = 500, message = "댓글 내용은 500자를 초과할 수 없습니다.")
        String commentContent
) {
}
