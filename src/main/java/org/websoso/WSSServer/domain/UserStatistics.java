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

    @Column(nullable = false, columnDefinition = "float default 0.0")
    private Float roNovelRatingSum;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer rfNovelNovelCount;

    @Column(nullable = false, columnDefinition = "float default 0.0")
    private Float rfNovelRatingSum;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer blNovelNovelCount;

    @Column(nullable = false, columnDefinition = "float default 0.0")
    private Float blNovelRatingSum;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer faNovelNovelCount;

    @Column(nullable = false, columnDefinition = "float default 0.0")
    private Float faNovelRatingSum;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer mfNovelNovelCount;

    @Column(nullable = false, columnDefinition = "float default 0.0")
    private Float mfNovelRatingSum;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer wuNovelNovelCount;

    @Column(nullable = false, columnDefinition = "float default 0.0")
    private Float wuNovelRatingSum;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer lnNovelNovelCount;

    @Column(nullable = false, columnDefinition = "float default 0.0")
    private Float lnNovelRatingSum;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer drNovelNovelCount;

    @Column(nullable = false, columnDefinition = "float default 0.0")
    private Float drNovelRatingSum;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer myNovelNovelCount;

    @Column(nullable = false, columnDefinition = "float default 0.0")
    private Float myNovelRatingSum;

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

    public void increaseRoNovelRatingSum(float rating) {
        this.roNovelRatingSum = Optional.ofNullable(this.roNovelRatingSum).orElse(0f) + rating;
    }

    public void increaseRfNovelNovelCount() {
        this.rfNovelNovelCount = Optional.ofNullable(this.rfNovelNovelCount).orElse(0) + 1;
    }

    public void increaseRfNovelRatingSum(float rating) {
        this.rfNovelRatingSum = Optional.ofNullable(this.rfNovelRatingSum).orElse(0f) + rating;
    }

    public void increaseBlNovelNovelCount() {
        this.blNovelNovelCount = Optional.ofNullable(this.blNovelNovelCount).orElse(0) + 1;
    }

    public void increaseBlNovelRatingSum(float rating) {
        this.blNovelRatingSum = Optional.ofNullable(this.blNovelRatingSum).orElse(0f) + rating;
    }

    public void increaseFaNovelNovelCount() {
        this.faNovelNovelCount = Optional.ofNullable(this.faNovelNovelCount).orElse(0) + 1;
    }

    public void increaseFaNovelRatingSum(float rating) {
        this.faNovelRatingSum = Optional.ofNullable(this.faNovelRatingSum).orElse(0f) + rating;
    }

    public void increaseMfNovelNovelCount() {
        this.mfNovelNovelCount = Optional.ofNullable(this.mfNovelNovelCount).orElse(0) + 1;
    }

    public void increaseMfNovelRatingSum(float rating) {
        this.mfNovelRatingSum = Optional.ofNullable(this.mfNovelRatingSum).orElse(0f) + rating;
    }

    public void increaseWuNovelNovelCount() {
        this.wuNovelNovelCount = Optional.ofNullable(this.wuNovelNovelCount).orElse(0) + 1;
    }

    public void increaseWuNovelRatingSum(float rating) {
        this.wuNovelRatingSum = Optional.ofNullable(this.wuNovelRatingSum).orElse(0f) + rating;
    }

    public void increaseLnNovelNovelCount() {
        this.lnNovelNovelCount = Optional.ofNullable(this.lnNovelNovelCount).orElse(0) + 1;
    }

    public void increaseLnNovelRatingSum(float rating) {
        this.lnNovelRatingSum = Optional.ofNullable(this.lnNovelRatingSum).orElse(0f) + rating;
    }

    public void increaseDrNovelNovelCount() {
        this.drNovelNovelCount = Optional.ofNullable(this.drNovelNovelCount).orElse(0) + 1;
    }

    public void increaseDrNovelRatingSum(float rating) {
        this.drNovelRatingSum = Optional.ofNullable(this.drNovelRatingSum).orElse(0f) + rating;
    }

    public void increaseMyNovelNovelCount() {
        this.myNovelNovelCount = Optional.ofNullable(this.myNovelNovelCount).orElse(0) + 1;
    }

    public void increaseMyNovelRatingSum(float rating) {
        this.myNovelRatingSum = Optional.ofNullable(this.myNovelRatingSum).orElse(0f) + rating;
    }

}