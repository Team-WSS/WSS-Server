package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;
import static org.websoso.WSSServer.exception.comment.CommentErrorCode.COMMENT_NOT_BELONG_TO_FEED;
import static org.websoso.WSSServer.exception.user.UserErrorCode.INVALID_AUTHORIZED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.websoso.WSSServer.domain.common.Action;
import org.websoso.WSSServer.domain.common.BaseEntity;
import org.websoso.WSSServer.exception.comment.exception.InvalidCommentException;
import org.websoso.WSSServer.exception.user.exception.InvalidAuthorizedException;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long commentId;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isHidden;

    @Column(columnDefinition = "varchar(100)", nullable = false)
    private String commentContent;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    public static Comment create(Long userId, Feed feed, String commentContent) {
        return new Comment(commentContent, userId, feed);
    }

    public void validateUserAuthorization(Long userId, Action action) {
        if (!Objects.equals(this.userId, userId)) {
            throw new InvalidAuthorizedException(INVALID_AUTHORIZED,
                    "only the author can " + action.getDescription() + " the comment");
        }
    }

    public void updateContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public void validateFeedAssociation(Feed feed) {
        if (this.feed != feed) {
            throw new InvalidCommentException(COMMENT_NOT_BELONG_TO_FEED,
                    "the comment does not belong to the specified feed");
        }
    }

    private Comment(String commentContent, Long userId, Feed feed) {
        this.commentContent = commentContent;
        this.userId = userId;
        this.feed = feed;
    }

}
