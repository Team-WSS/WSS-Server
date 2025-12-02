package org.websoso.WSSServer.feed.service;

import static org.websoso.WSSServer.exception.error.CustomCommentError.COMMENT_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.comment.CommentCreateRequest;
import org.websoso.WSSServer.dto.comment.CommentUpdateRequest;
import org.websoso.WSSServer.exception.exception.CustomCommentException;
import org.websoso.WSSServer.feed.domain.Comment;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.repository.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl {

    private final CommentRepository commentRepository;

    @Transactional
    public void createComment(User user, Feed feed, CommentCreateRequest request) {
        commentRepository.save(Comment.create(user.getUserId(), feed, request.commentContent()));
    }

    @Transactional(readOnly = true)
    public Comment findComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(
                () -> new CustomCommentException(COMMENT_NOT_FOUND, "comment with the given id was not found"));
    }

    @Transactional
    public void updateComment(Comment comment, CommentUpdateRequest request) {
        comment.updateContent(request.commentContent());
    }
}
