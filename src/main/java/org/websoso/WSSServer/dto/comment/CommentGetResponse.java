package org.websoso.WSSServer.dto.comment;

import org.websoso.WSSServer.feed.domain.Comment;
import org.websoso.WSSServer.dto.user.UserBasicInfo;
import org.websoso.WSSServer.util.TimeFormatUtil;

public record CommentGetResponse(
        Long userId,
        String nickname,
        String avatarImage,
        Long commentId,
        String createdDate,
        String commentContent,
        Boolean isModified,
        Boolean isMyComment,
        Boolean isSpoiler,
        Boolean isBlocked,
        Boolean isHidden
) {
    public static CommentGetResponse of(UserBasicInfo userBasicInfo, Comment comment, Boolean isMyComment,
                                        Boolean isSpoiler, Boolean isBlocked, Boolean isHidden) {
        return new CommentGetResponse(
                userBasicInfo.userId(),
                userBasicInfo.nickname(),
                userBasicInfo.avatarImage(),
                comment.getCommentId(),
                TimeFormatUtil.formatRelativeDateTime(comment.getCreatedDate()),
                comment.getCommentContent(),
                !comment.getCreatedDate().equals(comment.getModifiedDate()),
                isMyComment,
                isSpoiler,
                isBlocked,
                isHidden
        );
    }
}
