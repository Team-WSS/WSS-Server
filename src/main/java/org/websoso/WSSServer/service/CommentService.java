package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.Action.DELETE;
import static org.websoso.WSSServer.domain.common.Action.UPDATE;
import static org.websoso.WSSServer.exception.error.CustomCommentError.COMMENT_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Comment;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.exception.exception.CustomCommentException;
import org.websoso.WSSServer.repository.CommentRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;

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

    private Comment getCommentOrException(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
                new CustomCommentException(COMMENT_NOT_FOUND, "comment with the given id was not found"));
    }

}
