package org.websoso.WSSServer.notification.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.websoso.WSSServer.user.domain.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_read_notification_user",
                columnNames = {"notification_id", "user_id"}
        )
})
public class ReadNotification {

    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    @Comment("읽음 기록 고유 ID")
    private Long readNotificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    @Comment("읽은 알림 ID (FK)")
    private Notification notification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("알림을 읽은 사용자 ID (FK)")
    private User user;

    private ReadNotification(Notification notification, User user) {
        this.notification = notification;
        this.user = user;
    }

    public static ReadNotification create(Notification notification, User user) {
        return new ReadNotification(notification, user);
    }
}
