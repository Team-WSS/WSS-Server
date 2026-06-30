package org.websoso.WSSServer.feed.report.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.domain.common.ReportedType;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"feed_id", "user_id", "reported_type"})
})
public class ReportedFeed {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long reportedFeedId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportedType reportedType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private ReportedFeed(Feed feed, User user, ReportedType reportedType) {
        this.feed = feed;
        this.user = user;
        this.reportedType = reportedType;
    }

    public static ReportedFeed create(Feed feed, User user, ReportedType reportedType) {
        return new ReportedFeed(feed, user, reportedType);
    }

}
