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
public class BasicProfile {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Byte basicProfileId;

    @Column(columnDefinition = "varchar(10)", nullable = false)
    private String basicProfileName;

    @Column(columnDefinition = "varchar(50)", nullable = false)
    private String basicProfileLine;

    @Column(columnDefinition = "varchar(100)", nullable = false)
    private String basicProfileImage;
}
