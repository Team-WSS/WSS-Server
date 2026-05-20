package org.websoso.WSSServer.notification.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationType {

    @Id @GeneratedValue(strategy = IDENTITY)
    @Comment("알림 타입 ID (1:공지, 2:이벤트, 3:인기글, 4:댓글, 5:좋아요)")
    private Byte notificationTypeId;

    @Column(length = 10, nullable = false)
    @Comment("알림 타입 명칭 (예: 공지사항, 이벤트 등)")
    private String notificationTypeName;

    @Lob
    @Column(nullable = false)
    @Comment("알림 아이콘 이미지 경로 (AWS, S3 Path)")
    private String notificationTypeImage;

}
