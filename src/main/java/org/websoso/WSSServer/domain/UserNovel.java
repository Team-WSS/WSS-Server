package org.websoso.WSSServer.domain;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.websoso.WSSServer.domain.common.BaseEntity;
import org.websoso.WSSServer.domain.common.Flag;
import org.websoso.WSSServer.domain.common.ReadStatus;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNovel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long userNovelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'N'")
    private Flag isInterest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadStatus status;

    @Column(nullable = false, columnDefinition = "float default 0.0")
    private Float userNovelRating;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @OneToOne(mappedBy = "userNovel", cascade = ALL)
    private AttractivePoint attractivePoint;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;
}
