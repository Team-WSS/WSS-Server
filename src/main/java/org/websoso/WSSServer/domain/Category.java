package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private Boolean isRf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'N'")
    private Boolean isRo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'N'")
    private Boolean isFa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'N'")
    private Boolean isMf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'N'")
    private Boolean isDr;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'N'")
    private Boolean isLn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'N'")
    private Boolean isWu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'N'")
    private Boolean isMy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'N'")
    private Boolean isBl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'N'")
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
