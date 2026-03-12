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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long userDeviceId;

    @Column
    private String fcmToken;

    @Column(nullable = false)
    private String deviceIdentifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
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
