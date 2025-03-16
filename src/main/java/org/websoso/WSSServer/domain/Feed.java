package org.websoso.WSSServer.domain;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_AUTHORIZED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.websoso.WSSServer.domain.common.Action;
import org.websoso.WSSServer.exception.exception.CustomUserException;

@Getter
@DynamicInsert
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feed {

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

    @Column(nullable = false)
    private Boolean isSpoiler;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false)
    private LocalDateTime modifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "feed", cascade = ALL, fetch = FetchType.LAZY)
    private List<FeedCategory> feedCategories = new ArrayList<>();

    @OneToMany(mappedBy = "feed", cascade = ALL, fetch = FetchType.LAZY)
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "feed", cascade = ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "feed", cascade = ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ReportedFeed> reportedFeeds = new ArrayList<>();

    @OneToOne(mappedBy = "feed", cascade = ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PopularFeed popularFeed;

    private Feed(String feedContent, Long novelId, Boolean isSpoiler, User user) {
        this.feedContent = feedContent;
        this.novelId = novelId;
        this.isSpoiler = isSpoiler;
        this.user = user;
    }

    public static Feed create(String feedContent, Long novelId, Boolean isSpoiler, User user) {
        return new Feed(feedContent, novelId, isSpoiler, user);
    }

    public void updateFeed(String feedContent, Boolean isSpoiler, Long novelId) {
        this.feedContent = feedContent;
        this.isSpoiler = isSpoiler;
        this.novelId = novelId;
        this.modifiedDate = LocalDateTime.now();
    }

    public void validateUserAuthorization(User user, Action action) {
        if (!this.user.equals(user)) {
            throw new CustomUserException(INVALID_AUTHORIZED,
                    "only the author can " + action.getLabel() + " the feed");
        }
    }

    public boolean isNovelChanged(Long novelId) {
        return !Objects.equals(this.novelId, novelId);
    }

    public void hideFeed() {
        this.isHidden = true;
    }

}
