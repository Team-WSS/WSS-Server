package org.websoso.WSSServer.domain;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttractivePoint {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long attractivePointId;

    @Column(columnDefinition = "varchar(12)", nullable = false)
    private String attractivePointName;

    @OneToMany(mappedBy = "attractive_point", cascade = ALL, fetch = FetchType.LAZY)
    private List<UserNovelAttractivePoint> userNovelAttractivePoints = new ArrayList<>();

}
