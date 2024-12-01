package org.websoso.WSSServer.domain;

import static jakarta.persistence.EnumType.STRING;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.websoso.WSSServer.domain.common.OS;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MinimumVersion {

    @Id
    @Enumerated(STRING)
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private OS os;

    @Column(columnDefinition = "varchar(20)", nullable = false)
    private String minimumVersion;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updateDate;

    private MinimumVersion(OS os, String minimumVersion) {
        this.os = os;
        this.minimumVersion = minimumVersion;
        this.updateDate = LocalDateTime.now();
    }

    public static MinimumVersion create(OS os, String minimumVersion) {
        return new MinimumVersion(os, minimumVersion);
    }

    public void updateMinimumVersion(String minimumVersion) {
        this.minimumVersion = minimumVersion;
    }
}
