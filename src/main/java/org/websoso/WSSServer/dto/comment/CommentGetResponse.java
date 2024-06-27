package org.websoso.WSSServer.dto.comment;

import java.time.format.DateTimeFormatter;
import org.websoso.WSSServer.domain.Comment;
import org.websoso.WSSServer.dto.user.UserBasicInfo;

public record CommentGetResponse(
        Long userId,
        String nickname,
        String avatarImage,
        Long commentId,
        String createdDate,
        String commentContent,
        Boolean isModified,
        Boolean isMyComment
) {
    public static CommentGetResponse of(UserBasicInfo userBasicInfo, Comment comment, Boolean isMyComment) {
        return new CommentGetResponse(
                userBasicInfo.userId(),
                userBasicInfo.nickname(),
                userBasicInfo.avatarImage(),
                comment.getCommentId(),
                comment.getCreatedDate().format(DateTimeFormatter.ofPattern("M월 d일")),
                comment.getCommentContent(),
                !comment.getCreatedDate().equals(comment.getModifiedDate()),
                isMyComment
        );
    }
}
