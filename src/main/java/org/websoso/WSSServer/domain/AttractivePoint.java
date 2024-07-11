package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
    private Byte attractivePointId;

    @Column(columnDefinition = "varchar(12)", nullable = false)
    private String attractivePointName;

}
