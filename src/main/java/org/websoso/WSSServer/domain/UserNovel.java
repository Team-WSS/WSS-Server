package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.websoso.WSSServer.domain.common.BaseEntity;
import org.websoso.WSSServer.domain.common.ReadStatus;

@Entity
@Getter
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNovel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long userNovelId;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isInterest;

    @Enumerated(EnumType.STRING)
    @Column
    private ReadStatus status;

    @Column(nullable = false, columnDefinition = "float default 0.0")
    private Float userNovelRating;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
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

    public void setIsInterest(Boolean isInterest) {
        this.isInterest = isInterest;
    }

    public void deleteEvaluation() {
        this.userNovelRating = 0.0f;
        this.startDate = null;
        this.endDate = null;
    }

}
