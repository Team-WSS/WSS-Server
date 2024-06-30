package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;
import static org.websoso.WSSServer.exception.novelStatistics.NovelStatisticsErrorCode.INVALID_NOVEL_FEED_COUNT;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.websoso.WSSServer.exception.novelStatistics.exception.CustomNovelStatisticsException;

@DynamicInsert
@DynamicUpdate
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NovelStatistics {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long novelStatisticsId;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer novelFeedCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer watchingCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer interestCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer watchedCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer quitCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer universeCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer vibeCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer materialCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer charactersCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer relationshipCount;

    @OneToOne
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    @Builder
    public NovelStatistics(Novel novel) {
        this.novel = novel;
    }

    public void increaseNovelFeedCount() {
        this.novelFeedCount = Optional.ofNullable(this.novelFeedCount).orElse(0) + 1;
    }

    public void decreaseNovelFeedCount() {
        if (this.novelFeedCount <= 0) {
            throw new CustomNovelStatisticsException(INVALID_NOVEL_FEED_COUNT, "invalid novel feed count");
        }

        this.novelFeedCount--;
    }

    public void increaseWatchingCount() {
        this.watchingCount = Optional.ofNullable(this.watchingCount).orElse(0) + 1;
    }

    public void increaseInterestCount() {
        this.interestCount = Optional.ofNullable(this.interestCount).orElse(0) + 1;
    }

    public void increaseWatchedCount() {
        this.watchedCount = Optional.ofNullable(this.watchedCount).orElse(0) + 1;
    }

    public void increaseQuitCount() {
        this.quitCount = Optional.ofNullable(this.quitCount).orElse(0) + 1;
    }

    public void increaseUniverseCount() {
        this.universeCount = Optional.ofNullable(this.universeCount).orElse(0) + 1;
    }

    public void increaseVibeCount() {
        this.vibeCount = Optional.ofNullable(this.vibeCount).orElse(0) + 1;
    }

    public void increaseMaterialCount() {
        this.materialCount = Optional.ofNullable(this.materialCount).orElse(0) + 1;
    }

    public void increaseCharactersCount() {
        this.charactersCount = Optional.ofNullable(this.charactersCount).orElse(0) + 1;
    }

    public void increaseRelationshipCount() {
        this.relationshipCount = Optional.ofNullable(this.relationshipCount).orElse(0) + 1;
    }

}
