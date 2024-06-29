package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStatistics {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long userStatisticsId;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer watchingNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer interestNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer watchedNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer quitNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer roNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer rfNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer blNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer faNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer mfNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer wuNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer lnNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer drNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer myNovelCount;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void increaseWatchingNovelCount() {
        this.watchingNovelCount = Optional.ofNullable(this.watchingNovelCount).orElse(0) + 1;
    }

    public void increaseInterestNovelCount() {
        this.interestNovelCount = Optional.ofNullable(this.interestNovelCount).orElse(0) + 1;
    }

    public void increaseWatchedNovelCount() {
        this.watchedNovelCount = Optional.ofNullable(this.watchedNovelCount).orElse(0) + 1;
    }

    public void increaseQuitNovelCount() {
        this.quitNovelCount = Optional.ofNullable(this.quitNovelCount).orElse(0) + 1;
    }

    public void increaseRoNovelCount() {
        this.roNovelCount = Optional.ofNullable(this.roNovelCount).orElse(0) + 1;
    }

    public void increaseRfNovelCount() {
        this.rfNovelCount = Optional.ofNullable(this.rfNovelCount).orElse(0) + 1;
    }

    public void increaseBlNovelCount() {
        this.blNovelCount = Optional.ofNullable(this.blNovelCount).orElse(0) + 1;
    }

    public void increaseFaNovelCount() {
        this.faNovelCount = Optional.ofNullable(this.faNovelCount).orElse(0) + 1;
    }

    public void increaseMfNovelCount() {
        this.mfNovelCount = Optional.ofNullable(this.mfNovelCount).orElse(0) + 1;
    }

    public void increaseWuNovelCount() {
        this.wuNovelCount = Optional.ofNullable(this.wuNovelCount).orElse(0) + 1;
    }

    public void increaseLnNovelCount() {
        this.lnNovelCount = Optional.ofNullable(this.lnNovelCount).orElse(0) + 1;
    }

    public void increaseDrNovelCount() {
        this.drNovelCount = Optional.ofNullable(this.drNovelCount).orElse(0) + 1;
    }

    public void increaseMyNovelCount() {
        this.myNovelCount = Optional.ofNullable(this.myNovelCount).orElse(0) + 1;
    }

}