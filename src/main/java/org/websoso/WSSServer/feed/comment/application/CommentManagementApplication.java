package org.websoso.WSSServer.feed.comment.application;

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
    public void create(User user, Long feedId, CommentCreateRequest request) {

        Feed feed = feedServiceImpl.getAccessFeedOrException(feedId, user.getUserId());

        blockService.validateNotBlocked(user.getUserId(), feed.getWriterId());

        commentServiceImpl.createComment(user, feed, request);

        eventPublisher.publishEvent(CommentCreatedEvent.of(user.getUserId(), feed.getFeedId()));
    }

    @Transactional
    public void update(User user, Long commentId, CommentUpdateRequest request) {

        Comment comment = commentServiceImpl.getCommentOrException(commentId);

        comment.validateOwner(user.getUserId());

        comment.updateContent(request.commentContent());
    }

    @Transactional
    public void delete(User user, Long commentId) {

        Comment comment = commentServiceImpl.getCommentOrException(commentId);

        comment.validateOwner(user.getUserId());

        commentServiceImpl.deleteComment(comment);
    }

    @Deprecated(since = "PUT /comments/{commentId}으로 완벽 교체시")
    @Transactional
    public void update(User user, Long feedId, Long commentId, CommentUpdateRequest request) {

        Feed feed = feedServiceImpl.getAccessFeedOrException(feedId, user.getUserId());

        blockService.validateNotBlocked(user.getUserId(), feed.getWriterId());

        Comment comment = commentServiceImpl.getCommentOrException(commentId);

        comment.validateBelongsTo(feed);

        comment.validateOwner(user.getUserId());

        comment.updateContent(request.commentContent());
    }

    @Deprecated(since = "DELETE /comments/{commentId}으로 완벽 교체시")
    @Transactional
    public void delete(User user, Long feedId, Long commentId) {

        Feed feed = feedServiceImpl.getAccessFeedOrException(feedId, user.getUserId());

        blockService.validateNotBlocked(user.getUserId(), feed.getWriterId());

        Comment comment = commentServiceImpl.getCommentOrException(commentId);

        comment.validateBelongsTo(feed);

        comment.validateOwner(user.getUserId());

        commentServiceImpl.deleteComment(comment);
    }
}
