package org.websoso.WSSServer.validation;

import static org.websoso.WSSServer.domain.common.NotificationTypeGroup.NOTICE;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_NOT_NOTICE_TYPE;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.domain.common.NotificationTypeGroup;
import org.websoso.WSSServer.exception.exception.CustomNotificationException;

@Component
@RequiredArgsConstructor
public class NotificationTypeValidator implements ConstraintValidator<NotificationTypeConstraint, String> {

    @Override
    public void initialize(NotificationTypeConstraint notificationTypeName) {
    }

    @Override
    public boolean isValid(String notificationTypeName, ConstraintValidatorContext constraintValidatorContext) {
        if (!NotificationTypeGroup.isTypeInGroup(notificationTypeName, NOTICE)) {
            throw new CustomNotificationException(NOTIFICATION_NOT_NOTICE_TYPE,
                    "given notification type does not belong to the NOTICE category");
        }
        return true;
    }
}
