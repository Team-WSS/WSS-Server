package org.websoso.WSSServer.feed.comment.controller.dto;

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
