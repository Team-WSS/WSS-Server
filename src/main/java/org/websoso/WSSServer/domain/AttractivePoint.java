package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

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

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean universe;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean vibe;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean material;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean characters;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean relationship;

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
