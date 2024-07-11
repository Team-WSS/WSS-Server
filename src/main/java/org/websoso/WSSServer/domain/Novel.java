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
public class Novel {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long novelId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(columnDefinition = "text", nullable = false)
    private String novelImage;

    @Column(columnDefinition = "text", nullable = false)
    private String novelDescription;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isCompleted;

}
