package org.websoso.WSSServer.feed.domain;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.websoso.WSSServer.domain.User;

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

    @Column(columnDefinition = "Boolean default true", nullable = false)
    private Boolean isPublic;

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

    @OneToMany(cascade = ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "feed_id")
    @OrderBy("sequence ASC")
    private List<FeedImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "feed", cascade = ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ReportedFeed> reportedFeeds = new ArrayList<>();

    @OneToOne(mappedBy = "feed", cascade = ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PopularFeed popularFeed;

    private Feed(String feedContent, Long novelId, Boolean isSpoiler, Boolean isPublic, User user, List<FeedImage> images) {
        this.feedContent = feedContent;
        this.novelId = novelId;
        this.isSpoiler = isSpoiler;
        this.isPublic = isPublic;
        this.user = user;
        this.images = images;
        this.createdDate = LocalDateTime.now();
        this.modifiedDate = this.createdDate;
    }

    public static Feed create(String feedContent, Long novelId, Boolean isSpoiler, Boolean isPublic, User user, List<FeedImage> images) {
        return new Feed(feedContent, novelId, isSpoiler, isPublic, user, images);
    }

    public void updateFeed(String feedContent, Boolean isSpoiler, Boolean isPublic, Long novelId, List<FeedImage> images) {
        this.feedContent = feedContent;
        this.isSpoiler = isSpoiler;
        this.isPublic = isPublic;
        this.novelId = novelId;
        this.images.clear();
        this.images.addAll(images);
        this.modifiedDate = LocalDateTime.now();
    }

    public boolean isNovelChanged(Long novelId) {
        return !Objects.equals(this.novelId, novelId);
    }

    public void hideFeed() {
        this.isHidden = true;
    }

    public Long getWriterId() {
        return user.getUserId();
    }

    public boolean isMine(Long userId) {
        return this.getWriterId().equals(userId);
    }

    public boolean isVisibleTo(Long userId) {
        return this.isPublic || this.isMine(userId);
    }
}
