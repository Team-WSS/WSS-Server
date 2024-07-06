package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long categoryId;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isRf;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isRo;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isFa;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isMf;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isDr;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isLn;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isWu;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isMy;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isBl;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isEtc;

    @OneToOne
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @Builder
    public Category(Long categoryId, Boolean isRf, Boolean isRo, Boolean isFa, Boolean isMf, Boolean isDr, Boolean isLn,
                    Boolean isWu, Boolean isMy, Boolean isBl, Boolean isEtc, Feed feed) {
        this.categoryId = categoryId;
        this.isRf = isRf;
        this.isRo = isRo;
        this.isFa = isFa;
        this.isMf = isMf;
        this.isDr = isDr;
        this.isLn = isLn;
        this.isWu = isWu;
        this.isMy = isMy;
        this.isBl = isBl;
        this.isEtc = isEtc;
        this.feed = feed;
    }
}
