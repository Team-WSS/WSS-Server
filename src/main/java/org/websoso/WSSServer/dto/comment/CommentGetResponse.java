package org.websoso.WSSServer.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import org.websoso.WSSServer.domain.Comment;
import org.websoso.WSSServer.dto.user.UserBasicInfo;

public record CommentGetResponse(
        Long userId,
        String nickname,
        String avatarImage,
        Long commentId,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "M월 d일", timezone = "Asia/Seoul")
        LocalDate createdDate,
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
                isBlocked ? null : userBasicInfo.userId(),
                isBlocked ? null : userBasicInfo.nickname(),
                userBasicInfo.avatarImage(),
                comment.getCommentId(),
                comment.getCreatedDate().toLocalDate(),
                comment.getCommentContent(),
                !comment.getCreatedDate().equals(comment.getModifiedDate()),
                isMyComment,
                isSpoiler,
                isBlocked,
                isHidden
        );
    }
}
