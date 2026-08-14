package org.websoso.WSSServer.feed.comment.controller.dto;

import org.websoso.WSSServer.feed.comment.domain.Comment;
import org.websoso.WSSServer.dto.user.UserBasicInfo;
import org.websoso.WSSServer.util.TimeFormatUtil;
import org.websoso.support.logging.masking.MaskingPolicy;
import org.websoso.support.logging.masking.SensitiveData;

public record CommentGetResponse(
        Long userId,
        @SensitiveData(MaskingPolicy.NAME)
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
                comment.getContent(),
                !comment.getCreatedDate().equals(comment.getModifiedDate()),
                isMyComment,
                isSpoiler,
                isBlocked,
                isHidden
        );
    }
}
