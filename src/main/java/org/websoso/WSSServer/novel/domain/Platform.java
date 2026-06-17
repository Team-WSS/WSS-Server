package org.websoso.WSSServer.novel.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Platform {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Byte platformId;

    @Column(columnDefinition = "varchar(10)", nullable = false)
    private String platformName;

    @Column(columnDefinition = "text", nullable = false)
    private String platformImage;

    @OneToMany(mappedBy = "platform", fetch = FetchType.LAZY)
    private List<NovelPlatform> novelPlatforms = new ArrayList<>();

}
