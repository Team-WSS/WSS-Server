package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.Action.DELETE;
import static org.websoso.WSSServer.domain.common.Action.UPDATE;
import static org.websoso.WSSServer.exception.error.CustomCommentError.COMMENT_NOT_FOUND;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Comment;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.comment.CommentGetResponse;
import org.websoso.WSSServer.dto.comment.CommentsGetResponse;
import org.websoso.WSSServer.dto.user.UserBasicInfo;
import org.websoso.WSSServer.exception.exception.CustomCommentException;
import org.websoso.WSSServer.repository.CommentRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final AvatarService avatarService;
    private final BlockService blockService;

    public void createComment(Long userId, Feed feed, String commentContent) {
        commentRepository.save(Comment.create(userId, feed, commentContent));
    }

    public void updateComment(Long userId, Feed feed, Long commentId, String commentContent) {
        Comment comment = getCommentOrException(commentId);
        comment.validateFeedAssociation(feed);
        comment.validateUserAuthorization(userId, UPDATE);
        comment.updateContent(commentContent);
    }

    public void deleteComment(Long userId, Feed feed, Long commentId) {
        Comment comment = getCommentOrException(commentId);
        comment.validateFeedAssociation(feed);
        comment.validateUserAuthorization(userId, DELETE);
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public CommentsGetResponse getComments(User user, Feed feed) {
        List<Comment> comments = feed.getComments();
        List<CommentGetResponse> responses = new ArrayList<>();

        for (Comment comment : comments) {
            User createdUser = userService.getUserOrException(comment.getUserId());
            if (comment.getIsHidden() || isBlocked(createdUser, user)) {
                continue;
            }
            responses.add(CommentGetResponse.of(getUserBasicInfo(createdUser), comment,
                    isUserCommentOwner(createdUser, user)));
        }

        return CommentsGetResponse.of(comments.size(), responses);
    }

    private Comment getCommentOrException(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
                new CustomCommentException(COMMENT_NOT_FOUND, "comment with the given id was not found"));
    }

    private UserBasicInfo getUserBasicInfo(User user) {
        return user.getUserBasicInfo(
                avatarService.getAvatarOrException(user.getAvatarId()).getAvatarImage()
        );
    }

    private Boolean isUserCommentOwner(User createdUser, User user) {
        return createdUser.equals(user);
    }

    private Boolean isBlocked(User createdFeedUser, User user) {
        return blockService.isBlockedRelationship(user.getUserId(), createdFeedUser.getUserId());
    }

}
