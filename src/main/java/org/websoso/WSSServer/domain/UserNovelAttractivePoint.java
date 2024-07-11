package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNovelAttractivePoint {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long userNovelAttractivePointId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_novel_id", nullable = false)
    private UserNovel userNovel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attractive_point_id", nullable = false)
    private AttractivePoint attractivePoint;

    private UserNovelAttractivePoint(UserNovel userNovel, AttractivePoint attractivePoint) {
        this.userNovel = userNovel;
        this.attractivePoint = attractivePoint;
    }

    public static UserNovelAttractivePoint create(UserNovel userNovel, AttractivePoint attractivePoint) {
        return new UserNovelAttractivePoint(userNovel, attractivePoint);
    }

}
