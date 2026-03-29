package org.websoso.WSSServer.notification.domain;

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
import org.hibernate.annotations.Comment;
import org.websoso.common.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    @Comment("알림 고유 ID")
    private Long notificationId;

    @Column(length = 200, nullable = false)
    @Comment("푸시 알림 제목")
    private String notificationTitle;

    @Column(length = 200, nullable = false)
    @Comment("푸시 알림 본문 내용")
    private String notificationBody;

    @Column(length = 2000)
    @Comment("알림 클릭 시 보여줄 상세 메시지 (공지사항용)")
    private String notificationDetail;

    @Column(nullable = false)
    @Comment("수신자 ID (0: 전체 공지, 그 외: 특정 사용자 ID)")
    private Long userId;

    @Column
    @Comment("관련 피드 ID (댓글/좋아요 등 피드 이동 시 사용)")
    private Long feedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_type_id", nullable = false)
    @Comment("알림 유형 (1:공지, 2:이벤트, 3:인기글, 4:댓글, 5:좋아요)")
    private NotificationType notificationType;

    private Notification(String title, String body, String detail, Long userId, Long feedId, NotificationType type) {
        this.notificationTitle = title;
        this.notificationBody = body;
        this.notificationDetail = detail;
        this.userId = userId;
        this.feedId = feedId;
        this.notificationType = type;
    }

    @Deprecated
    public static Notification create(String title, String body, String detail, Long userId, Long feedId, NotificationType type) {
        return new Notification(title, body, detail, userId, feedId, type);
    }

    /**
     * 피드 관련 알림 생성 (댓글, 좋아요 등)
     */
    public static Notification createFeedNotification(String title, String body, Long userId, Long feedId, NotificationType type) {
        return new Notification(title, body, null, userId, feedId, type);
    }

    /**
     * 공지사항 알림 생성 (전체 또는 개인)
     */
    public static Notification createNoticeNotification(String title, String body, String detail, Long userId, NotificationType type) {
        return new Notification(title, body, detail, userId, null, type);
    }

}
