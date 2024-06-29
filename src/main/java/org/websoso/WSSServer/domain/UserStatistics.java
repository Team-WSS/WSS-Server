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
    private Integer roNovelNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer rfNovelNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer blNovelNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer faNovelNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer mfNovelNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer wuNovelNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer lnNovelNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer drNovelNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer myNovelNovelCount;

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

    public void increaseRoNovelNovelCount() {
        this.roNovelNovelCount = Optional.ofNullable(this.roNovelNovelCount).orElse(0) + 1;
    }

    public void increaseRfNovelNovelCount() {
        this.rfNovelNovelCount = Optional.ofNullable(this.rfNovelNovelCount).orElse(0) + 1;
    }

    public void increaseBlNovelNovelCount() {
        this.blNovelNovelCount = Optional.ofNullable(this.blNovelNovelCount).orElse(0) + 1;
    }

    public void increaseFaNovelNovelCount() {
        this.faNovelNovelCount = Optional.ofNullable(this.faNovelNovelCount).orElse(0) + 1;
    }

    public void increaseMfNovelNovelCount() {
        this.mfNovelNovelCount = Optional.ofNullable(this.mfNovelNovelCount).orElse(0) + 1;
    }

    public void increaseWuNovelNovelCount() {
        this.wuNovelNovelCount = Optional.ofNullable(this.wuNovelNovelCount).orElse(0) + 1;
    }

    public void increaseLnNovelNovelCount() {
        this.lnNovelNovelCount = Optional.ofNullable(this.lnNovelNovelCount).orElse(0) + 1;
    }

    public void increaseDrNovelNovelCount() {
        this.drNovelNovelCount = Optional.ofNullable(this.drNovelNovelCount).orElse(0) + 1;
    }

    public void increaseMyNovelNovelCount() {
        this.myNovelNovelCount = Optional.ofNullable(this.myNovelNovelCount).orElse(0) + 1;
    }

}