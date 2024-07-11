package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;
import static org.websoso.WSSServer.exception.error.CustomFeedError.ALREADY_LIKED;
import static org.websoso.WSSServer.exception.error.CustomFeedError.INVALID_LIKE_COUNT;
import static org.websoso.WSSServer.exception.error.CustomFeedError.LIKE_USER_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_AUTHORIZED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.websoso.WSSServer.domain.common.Action;
import org.websoso.WSSServer.domain.common.BaseEntity;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.exception.exception.CustomUserException;

@Getter
@DynamicInsert
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feed extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long feedId;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isHidden;

    @Column(columnDefinition = "varchar(2000)", nullable = false)
    private String feedContent;

    @Column
    private Long novelId;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isSpoiler;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Feed(String feedContent, Boolean isSpoiler, Long novelId, User user) {
        this.feedContent = feedContent;
        this.isSpoiler = isSpoiler;
        this.novelId = novelId;
        this.user = user;
    }

    public void updateFeed(String feedContent, Boolean isSpoiler, Long novelId) {
        this.feedContent = feedContent;
        this.isSpoiler = isSpoiler;
        this.novelId = novelId;
    }

    public void validateUserAuthorization(User user, Action action) {
        if (!this.user.equals(user)) {
            throw new CustomUserException(INVALID_AUTHORIZED,
                    "only the author can " + action.getDescription() + " the feed");
        }
    }

    public void addLike(String likeUserId) {
        String likeUserIdFormatted = "{" + likeUserId + "}";

        if (this.likeUsers.contains(likeUserIdFormatted)) {
            throw new CustomFeedException(ALREADY_LIKED, "already liked feed");
        }

        this.likeUsers += likeUserIdFormatted;
        this.likeCount++;
    }

    public void unLike(String unLikeUserId) {
        String unLikeUserIdFormatted = "{" + unLikeUserId + "}";

        if (!this.likeUsers.contains(unLikeUserIdFormatted)) {
            throw new CustomFeedException(LIKE_USER_NOT_FOUND, "user has not liked this feed");
        }

        if (this.likeCount <= 0) {
            throw new CustomFeedException(INVALID_LIKE_COUNT, "invalid like count");
        }

        this.likeUsers = this.likeUsers.replace(unLikeUserIdFormatted, "");
        this.likeCount--;
    }

    public boolean isNovelLinked() {
        return this.novelId != null;
    }

    public boolean isNovelChanged(Long novelId) {
        return !Objects.equals(this.novelId, novelId);
    }

}
