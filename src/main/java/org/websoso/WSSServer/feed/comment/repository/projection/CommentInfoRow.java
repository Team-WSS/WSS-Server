package org.websoso.WSSServer.feed.comment.repository.projection;

import java.time.LocalDateTime;
import org.websoso.WSSServer.feed.comment.controller.dto.CommentGetResponse;
import org.websoso.WSSServer.util.TimeFormatUtil;

public record CommentInfoRow(
        Long userId,
        String nickname,
        String avatarImage,
        Long commentId,
        LocalDateTime createdDate,
        String commentContent,
        Boolean isModified,
        Boolean isMyComment,
        Boolean isSpoiler,
        Boolean isBlocked,
        Boolean isHidden
) {

    public CommentGetResponse toResponse() {
        return new CommentGetResponse(
                userId,
                nickname,
                avatarImage,
                commentId,
                TimeFormatUtil.formatRelativeDateTime(createdDate),
                commentContent,
                isModified,
                isMyComment,
                isSpoiler,
                isBlocked,
                isHidden
        );
    }
}
