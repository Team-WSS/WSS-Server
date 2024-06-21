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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.websoso.WSSServer.domain.common.Flag;

@Entity
@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttractivePoint {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long attractivePointId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'N'")
    private Flag universe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'N'")
    private Flag vibe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'N'")
    private Flag material;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'N'")
    private Flag characters;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'N'")
    private Flag relationship;

    @OneToOne
    @JoinColumn(name = "user_novel_id", nullable = false)
    private UserNovel userNovel;

    private AttractivePoint(UserNovel userNovel) {
        this.userNovel = userNovel;
    }

    public static AttractivePoint create(UserNovel userNovel) {
        return new AttractivePoint(userNovel);
    }
}
