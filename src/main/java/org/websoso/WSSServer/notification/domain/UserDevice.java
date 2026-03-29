package org.websoso.WSSServer.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
                name = "uk_user_device_identifier",
                columnNames = {"user_id", "device_identifier"}
        )
})
public class UserDevice {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    @Comment("사용자 기기 관리 고유 ID")
    private Long userDeviceId;

    @Column
    @Comment("FCM 푸시 알림 발송용 토큰")
    private String fcmToken;

    @Column(nullable = false)
    @Comment("기기 고유 식별값")
    private String deviceIdentifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("기기 소유 사용자 ID (FK)")
    private User user;

    private UserDevice(String fcmToken, String deviceIdentifier, User user) {
        this.fcmToken = fcmToken;
        this.deviceIdentifier = deviceIdentifier;
        this.user = user;
    }

    public static UserDevice create(String fcmToken, String deviceIdentifier, User user) {
        return new UserDevice(fcmToken, deviceIdentifier, user);
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
