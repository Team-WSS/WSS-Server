package org.websoso.WSSServer.feed.domain;

import static jakarta.persistence.GenerationType.IDENTITY;
import static org.websoso.WSSServer.exception.error.CustomCommentError.COMMENT_NOT_BELONG_TO_FEED;
import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_AUTHORIZED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.websoso.WSSServer.domain.common.Action;
import org.websoso.WSSServer.exception.exception.CustomCommentException;
import org.websoso.WSSServer.exception.exception.CustomUserException;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long commentId;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isHidden;

    @Column(columnDefinition = "varchar(500)", nullable = false)
    private String commentContent;

    @Column(nullable = false)
    private Long userId;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isSpoiler;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false)
    private LocalDateTime modifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    public static Comment create(Long userId, Feed feed, String commentContent) {
        return new Comment(commentContent, userId, feed);
    }

    public void validateUserAuthorization(Long userId, Action action) {
        if (!Objects.equals(this.userId, userId)) {
            throw new CustomUserException(INVALID_AUTHORIZED,
                    "only the author can " + action.getLabel() + " the comment");
        }
    }

    public void updateContent(String commentContent) {
        this.commentContent = commentContent;
        this.modifiedDate = LocalDateTime.now();
    }

    public void validateFeedAssociation(Feed feed) {
        if (this.feed != feed) {
            throw new CustomCommentException(COMMENT_NOT_BELONG_TO_FEED,
                    "the comment does not belong to the specified feed");
        }
    }

    private Comment(String commentContent, Long userId, Feed feed) {
        this.commentContent = commentContent;
        this.userId = userId;
        this.feed = feed;
        this.createdDate = LocalDateTime.now();
        this.modifiedDate = this.createdDate;
    }

    public void hideComment() {
        this.isHidden = true;
    }

    public void spoiler() {
        this.isSpoiler = true;
    }

    public boolean isMine(Long userId) {
        return this.getUserId().equals(userId);
    }
}
