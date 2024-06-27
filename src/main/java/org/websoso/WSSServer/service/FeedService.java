package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.Action.DELETE;
import static org.websoso.WSSServer.domain.common.Action.UPDATE;
import static org.websoso.WSSServer.exception.feed.FeedErrorCode.BLOCKED_USER_ACCESS;
import static org.websoso.WSSServer.exception.feed.FeedErrorCode.FEED_NOT_FOUND;
import static org.websoso.WSSServer.exception.feed.FeedErrorCode.HIDDEN_FEED_ACCESS;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.comment.CommentCreateRequest;
import org.websoso.WSSServer.dto.comment.CommentUpdateRequest;
import org.websoso.WSSServer.dto.comment.CommentsGetResponse;
import org.websoso.WSSServer.dto.feed.FeedCreateRequest;
import org.websoso.WSSServer.dto.feed.FeedUpdateRequest;
import org.websoso.WSSServer.exception.feed.exception.InvalidFeedException;
import org.websoso.WSSServer.repository.FeedRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedService {

    private final FeedRepository feedRepository;
    private final CategoryService categoryService;
    private final NovelStatisticsService novelStatisticsService;
    private final NovelService novelService;
    private final CommentService commentService;
    private final BlockService blockService;

    public void createFeed(User user, FeedCreateRequest request) {
        Feed feed = Feed.builder()
                .feedContent(request.feedContent())
                .isSpoiler(request.isSpoiler())
                .novelId(request.novelId())
                .user(user)
                .build();

        if (request.novelId() != null) {
            novelStatisticsService.increaseNovelFeedCount(novelService.getNovelOrException(request.novelId()));
        }

        feedRepository.save(feed);
        categoryService.createCategory(feed, request.relevantCategories());
    }

    public void updateFeed(User user, Long feedId, FeedUpdateRequest request) {
        Feed feed = getFeedOrException(feedId);

        feed.validateUserAuthorization(user, UPDATE);
        
        if (feed.isNovelChanged(request.novelId())) {
            if (feed.isNovelLinked()) {
                novelStatisticsService.decreaseNovelFeedCount(novelService.getNovelOrException(feed.getNovelId()));
            }
            if (request.novelId() != null) {
                novelStatisticsService.increaseNovelFeedCount(novelService.getNovelOrException(request.novelId()));
            }
        }

        feed.updateFeed(request.feedContent(), request.isSpoiler(), request.novelId());
        categoryService.updateCategory(feed, request.relevantCategories());
    }

    public void deleteFeed(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);

        feed.validateUserAuthorization(user, DELETE);

        if (feed.getNovelId() != null) {
            novelStatisticsService.decreaseNovelFeedCount(novelService.getNovelOrException(feed.getNovelId()));
        }

        feedRepository.delete(feed);
    }

    public void likeFeed(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);

        String likeUserId = String.valueOf(user.getUserId());

        feed.addLike(likeUserId);
    }

    public void unLikeFeed(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);

        String unLikeUserId = String.valueOf(user.getUserId());

        feed.unLike(unLikeUserId);
    }

    public void createComment(User user, Long feedId, CommentCreateRequest request) {
        Feed feed = getFeedOrException(feedId);

        if (!feed.getUser().equals(user)) {
            isHiddenFeed(feed);
            isBlockedRelationship(feed.getUser(), user);
        }

        commentService.createComment(user.getUserId(), feed, request.commentContent());

        feed.incrementCommentCount();
    }

    public void updateComment(User user, Long feedId, Long commentId, CommentUpdateRequest request) {
        Feed feed = getFeedOrException(feedId);

        if (!feed.getUser().equals(user)) {
            isHiddenFeed(feed);
            isBlockedRelationship(feed.getUser(), user);
        }

        commentService.updateComment(user.getUserId(), feed, commentId, request.commentContent());
    }

    public void deleteComment(User user, Long feedId, Long commentId) {
        Feed feed = getFeedOrException(feedId);

        if (!feed.getUser().equals(user)) {
            isHiddenFeed(feed);
            isBlockedRelationship(feed.getUser(), user);
        }

        commentService.deleteComment(user.getUserId(), feed, commentId);

        feed.decrementCommentCount();
    }

    @Transactional(readOnly = true)
    public CommentsGetResponse getComments(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);

        if (!feed.getUser().equals(user)) {
            isHiddenFeed(feed);
            isBlockedRelationship(feed.getUser(), user);
        }

        return commentService.getComments(user, feed);
    }

    private Feed getFeedOrException(Long feedId) {
        return feedRepository.findById(feedId).orElseThrow(() ->
                new InvalidFeedException(FEED_NOT_FOUND, "feed with the given id was not found"));
    }

    private void isHiddenFeed(Feed feed) {
        if (feed.getIsHidden()) {
            throw new InvalidFeedException(HIDDEN_FEED_ACCESS, "Cannot access hidden feed.");
        }
    }

    private void isBlockedRelationship(User createdFeedUser, User user) {
        if (blockService.isBlockedRelationship(user.getUserId(), createdFeedUser.getUserId())) {
            throw new InvalidFeedException(BLOCKED_USER_ACCESS,
                    "cannot access this feed because either you or the feed author has blocked the other.");
        }
    }

}
