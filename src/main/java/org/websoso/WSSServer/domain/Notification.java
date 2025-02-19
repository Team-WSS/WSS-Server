package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.websoso.WSSServer.domain.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long notificationId;

    @Column(columnDefinition = "varchar(200)", nullable = false)
    private String notificationTitle;

    @Column(columnDefinition = "varchar(200)", nullable = false)
    private String notificationBody;

    @Column(columnDefinition = "varchar(2000)")
    private String notificationDetail;

    @Column(nullable = false)
    private Long userId;

    private Long feedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_type_id", nullable = false)
    private NotificationType notificationType;

    private Notification(String notificationTitle, String notificationBody, String notificationDetail,
                         Long userId, Long feedId, NotificationType notificationType) {
        this.notificationTitle = notificationTitle;
        this.notificationBody = notificationBody;
        this.notificationDetail = notificationDetail;
        this.userId = userId;
        this.feedId = feedId;
        this.notificationType = notificationType;
    }

    public static Notification create(String notificationTitle, String notificationBody,
                                      String notificationDetail, Long userId, Long feedId,
                                      NotificationType notificationType) {
        return new Notification(notificationTitle, notificationBody, notificationDetail, userId, feedId,
                notificationType);
    }
}
