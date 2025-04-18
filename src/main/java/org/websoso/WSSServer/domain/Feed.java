package org.websoso.WSSServer.domain;

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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.websoso.WSSServer.dto.feed.FeedCreateRequest;

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

    @OneToMany(mappedBy = "feed", cascade = ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ReportedFeed> reportedFeeds = new ArrayList<>();

    @OneToOne(mappedBy = "feed", cascade = ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PopularFeed popularFeed;

    private Feed(FeedCreateRequest request, User user) {
        this.feedContent = request.feedContent();
        this.novelId = request.novelId();
        this.isSpoiler = request.isSpoiler();
        this.isPublic = request.isPublic();
        this.user = user;
    }

    public static Feed create(FeedCreateRequest request, User user) {
        return new Feed(request, user);
    }

    public void updateFeed(String feedContent, Boolean isSpoiler, Long novelId) {
        this.feedContent = feedContent;
        this.isSpoiler = isSpoiler;
        this.novelId = novelId;
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
        return this.user.getUserId().equals(userId);
    }
}
