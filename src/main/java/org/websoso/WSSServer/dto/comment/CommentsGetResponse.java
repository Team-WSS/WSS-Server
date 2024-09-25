package org.websoso.WSSServer.dto.comment;

import java.util.List;

public record CommentsGetResponse(
        Integer commentsCount,
        List<CommentGetResponse> comments
) {
    public static CommentsGetResponse of(final List<CommentGetResponse> comments) {
        return new CommentsGetResponse(
                comments.size(),
                comments
        );
    }
}
