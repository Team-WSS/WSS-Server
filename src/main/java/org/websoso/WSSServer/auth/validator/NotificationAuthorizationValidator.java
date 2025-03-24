package org.websoso.WSSServer.auth.validator;

import static org.websoso.WSSServer.domain.common.Role.ADMIN;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_ADMIN_ONLY;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.domain.Notification;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.exception.exception.CustomNotificationException;
import org.websoso.WSSServer.repository.NotificationRepository;

@Component
@RequiredArgsConstructor
public class NotificationAuthorizationValidator implements ResourceAuthorizationValidator {

    private final NotificationRepository notificationRepository;

    @Override
    public boolean hasPermission(Long resourceId, User user) {
        if (resourceId == null) {
            return isAdmin(user);
        }

        Notification notification = getNotificationOrException(resourceId);
        return isAdmin(user);
    }

    private Notification getNotificationOrException(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomNotificationException(NOTIFICATION_NOT_FOUND,
                        "notification with the given id is not found"));
    }

    private boolean isAdmin(User user) {
        if (user.getRole() != ADMIN) {
            throw new CustomNotificationException(NOTIFICATION_ADMIN_ONLY,
                    "User who tried to create, modify, or delete the notice is not an ADMIN.");
        }
        return true;
    }

    @Override
    public Class<?> getResourceType() {
        return Notification.class;
    }
}
