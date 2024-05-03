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
public class ReportedFeed {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long reportedFeedId;

    @Column(nullable = false)
    private Long feedId;

    @Column(columnDefinition = "tinyint default 0", nullable = false)
    private Byte spoilerCount;

    @Column(columnDefinition = "tinyint default 0", nullable = false)
    private Byte impertinenceCount;
}
