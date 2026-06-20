package org.websoso.WSSServer.feed.comment.application;

import static org.websoso.WSSServer.domain.common.Action.DELETE;
import static org.websoso.WSSServer.domain.common.Action.UPDATE;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.feed.comment.controller.dto.CommentCreateRequest;
import org.websoso.WSSServer.feed.comment.controller.dto.CommentUpdateRequest;
import org.websoso.WSSServer.feed.comment.domain.Comment;
import org.websoso.WSSServer.feed.comment.event.CommentCreatedEvent;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.comment.service.CommentServiceImpl;
import org.websoso.WSSServer.feed.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.user.service.BlockService;

@Service
@RequiredArgsConstructor
public class CommentManagementApplication {

    private final CommentServiceImpl commentServiceImpl;
    private final FeedServiceImpl feedServiceImpl;
    private final BlockService blockService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createComment(User user, Long feedId, CommentCreateRequest request) {

        Feed feed = feedServiceImpl.getAccessFeedOrException(feedId, user.getUserId());

        blockService.validateNotBlocked(user.getUserId(), feed.getWriterId());

        commentServiceImpl.createComment(user, feed, request);

        eventPublisher.publishEvent(CommentCreatedEvent.of(user.getUserId(), feed.getFeedId()));
    }

    @Transactional
    public void updateComment(User user, Long feedId, Long commentId, CommentUpdateRequest request) {
        Feed feed = feedServiceImpl.getFeedOrException(feedId);
        Comment comment = commentServiceImpl.findComment(commentId);
        comment.validateFeedAssociation(feed);
        comment.validateUserAuthorization(user.getUserId(), UPDATE);
        commentServiceImpl.updateComment(comment, request);
    }

    @Transactional
    public void deleteComment(User user, Long feedId, Long commentId) {
        Feed feed = feedServiceImpl.getFeedOrException(feedId);
        Comment comment = commentServiceImpl.findComment(commentId);
        comment.validateFeedAssociation(feed);
        comment.validateUserAuthorization(user.getUserId(), DELETE);
        commentServiceImpl.deleteComment(comment);
    }
}
