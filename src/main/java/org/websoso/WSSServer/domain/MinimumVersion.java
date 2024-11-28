package org.websoso.WSSServer.domain;

import static jakarta.persistence.EnumType.STRING;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.websoso.WSSServer.domain.common.OS;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MinimumVersion {

    @Id
    @Enumerated(STRING)
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private OS os;

    @Column(columnDefinition = "varchar(20)", nullable = false)
    private String minimumVersion;

    @Column(nullable = false)
    private LocalDateTime updateDate;
}
