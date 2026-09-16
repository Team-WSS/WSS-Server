package org.websoso.WSSServer.notification.domain;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.common.entity.BaseEntity;

/** 사용자가 작품별로 등록한 완결·휴재 복귀 알림을 저장한다. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "novel_notification_subscription",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_novel_notification_subscription_user_novel_type",
                columnNames = {"user_id", "novel_id", "notification_type"}
        ),
        indexes = {
                @Index(
                        name = "idx_novel_notification_subscription_user_type_sent_id",
                        columnList = "user_id, notification_type, is_sent, novel_notification_subscription_id"
                ),
                @Index(
                        name = "idx_novel_notification_subscription_novel_type",
                        columnList = "novel_id, notification_type"
                )
        }
)
public class NovelNotificationSubscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long novelNotificationSubscriptionId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = CASCADE)
    private User user;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    @OnDelete(action = CASCADE)
    private Novel novel;

    @Enumerated(STRING)
    @Column(name = "notification_type", length = 30, nullable = false)
    private NovelNotificationType notificationType;

    /**
     * 알림 발송 여부. 등록 시 false이고, 어드민이 발송을 마치면 true가 된다.
     * 발송은 이 서버가 아니라 어드민에서 처리하므로 여기서는 값을 바꾸지 않는다.
     */
    @Column(name = "is_sent", nullable = false)
    private boolean isSent;

    /** 구독의 소유자와 대상 작품, 알림 유형을 초기화한다. 발송 여부는 미발송으로 시작한다. */
    private NovelNotificationSubscription(User user, Novel novel, NovelNotificationType notificationType) {
        this.user = user;
        this.novel = novel;
        this.notificationType = notificationType;
        this.isSent = false;
    }

    /** 사용자와 작품, 알림 유형으로 새 구독을 생성한다. */
    public static NovelNotificationSubscription create(
            User user,
            Novel novel,
            NovelNotificationType notificationType
    ) {
        return new NovelNotificationSubscription(user, novel, notificationType);
    }
}
