package org.websoso.WSSServer.auth.validator;

import static org.websoso.WSSServer.exception.error.CustomCommentError.COMMENT_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_AUTHORIZED;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.domain.Comment;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.exception.exception.CustomCommentException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.repository.CommentRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentAuthorizationValidator implements ResourceAuthorizationValidator {

    private final CommentRepository commentRepository;

    @Override
    public boolean hasPermission(Long commentId, User user) {
        Comment comment = getCommentOrException(commentId);

        if (!comment.isMine(user.getUserId())) {
            throw new CustomUserException(INVALID_AUTHORIZED,
                    "User with ID " + user.getUserId() + " is not the owner of comment " + comment.getCommentId());
        }
        return true;
    }

    private Comment getCommentOrException(Long commentId) {
        log.info("### commentId: " + commentId);
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomCommentException(COMMENT_NOT_FOUND,
                        "comment with the given id was not found"));
    }

    @Override
    public Class<?> getResourceType() {
        return Comment.class;
    }
}
