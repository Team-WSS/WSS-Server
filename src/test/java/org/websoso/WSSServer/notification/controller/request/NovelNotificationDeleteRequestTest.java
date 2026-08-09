package org.websoso.WSSServer.notification.controller.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.NOVEL_ID_NOT_NULL;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.websoso.WSSServer.notification.domain.NovelNotificationType;

/** 작품 알림 일괄 삭제 요청의 작품 ID 요소 검증을 확인한다. */
class NovelNotificationDeleteRequestTest {

    /** 작품 ID 목록에 null이 포함되면 검증에 실패하는지 확인한다. */
    @Test
    @DisplayName("삭제할 작품 ID에 null을 허용하지 않는다")
    void rejectsNullNovelId() {
        NovelNotificationDeleteRequest request = new NovelNotificationDeleteRequest(
                NovelNotificationType.COMPLETION,
                Arrays.asList(10L, null)
        );

        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            Set<ConstraintViolation<NovelNotificationDeleteRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains(NOVEL_ID_NOT_NULL);
        }
    }
}
