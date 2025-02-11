package org.websoso.WSSServer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.repository.FeedRepository;

@Component
@AllArgsConstructor
public class FeedIdValidator implements ConstraintValidator<FeedIdConstraint, Long> {

    private final FeedRepository feedRepository;

    @Override
    public void initialize(FeedIdConstraint feedId) {
    }

    @Override
    public boolean isValid(Long feedId, ConstraintValidatorContext constraintValidatorContext) {
        if (feedId == null) {
            return true;
        }
        return feedRepository.existsById(feedId);
    }
}
