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
public class Avatar {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Byte avatarId;

    @Column(columnDefinition = "varchar(10)", nullable = false)
    private String avatarName;

    @Column(columnDefinition = "varchar(100)", nullable = false)
    private String avatarImage;

    @OneToMany(mappedBy = "avatar", cascade = ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<AvatarLine> avatarLine = new ArrayList<>();

}
