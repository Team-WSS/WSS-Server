package org.websoso.WSSServer.feed.comment.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.feed.comment.controller.dto.CommentGetResponse;
import org.websoso.WSSServer.feed.comment.controller.dto.CommentsGetResponse;
import org.websoso.WSSServer.feed.comment.service.CommentQueryService;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.user.service.BlockService;

@Service
@RequiredArgsConstructor
public class CommentFindApplication {

    private final FeedServiceImpl feedServiceImpl;
    private final BlockService blockService;
    private final CommentQueryService commentQueryService;

    @Transactional(readOnly = true)
    public CommentsGetResponse getComments(User user, Long feedId) {

        Feed feed = feedServiceImpl.getAccessFeedOrException(feedId, user.getUserId());

        blockService.validateNotBlocked(user.getUserId(), feed.getWriterId());

        List<Long> blockedUserIds = blockService.findBlockRelationUserIds(user.getUserId());
        List<CommentGetResponse> responses = commentQueryService.findCommentRows(
                feedId,
                user.getUserId(),
                blockedUserIds
        );

        return CommentsGetResponse.of(responses);
    }
}
