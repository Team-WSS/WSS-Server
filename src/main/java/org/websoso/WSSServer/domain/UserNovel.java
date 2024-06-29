package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.websoso.WSSServer.domain.common.BaseEntity;
import org.websoso.WSSServer.domain.common.Flag;
import org.websoso.WSSServer.domain.common.ReadStatus;

@Entity
@Getter
@Setter
@DynamicInsert
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
    @Column
    private ReadStatus status;

    @Column(nullable = false, columnDefinition = "float default 0.0")
    private Float userNovelRating;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    private UserNovel(ReadStatus status, Float userNovelRating, LocalDate startDate, LocalDate endDate, User user,
                      Novel novel) {
        this.status = status;
        this.userNovelRating = userNovelRating;
        this.startDate = startDate;
        this.endDate = endDate;
        this.user = user;
        this.novel = novel;
    }

    public static UserNovel create(ReadStatus status, Float userNovelRating, LocalDate startDate, LocalDate endDate,
                                   User user, Novel novel) {
        return new UserNovel(status, userNovelRating, startDate, endDate, user, novel);
    }

}
