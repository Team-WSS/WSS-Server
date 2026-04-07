<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# validation

## Purpose
커스텀 Bean Validation 어노테이션 및 Validator 구현체. 생년월일, 성별, 닉네임, 유저 ID, 알림 타입 등 도메인 특화 입력 유효성 검증 로직을 재사용 가능한 어노테이션으로 캡슐화합니다.

## Key Files

| File | Description |
|------|-------------|
| `BirthConstraint.java` / `BirthValidator.java` | 생년월일 형식 검증 |
| `GenderConstraint.java` / `GenderValidator.java` | 성별 값 검증 |
| `NicknameConstraint.java` / `NicknameValidator.java` | 닉네임 규칙 검증 |
| `NullAllowedNicknameConstraint.java` / `NullAllowedNicknameValidator.java` | null 허용 닉네임 검증 |
| `BlockIdConstraint.java` / `BlockIdValidator.java` | 차단 대상 유저 ID 검증 |
| `UserIdConstraint.java` / `UserIdValidator.java` | 유저 ID 검증 |
| `ZeroAllowedUserIdConstraint.java` / `ZeroAllowedUserIdValidator.java` | 0 허용 유저 ID 검증 |
| `NotificationTypeConstraint.java` / `NotificationTypeValidator.java` | 알림 타입 검증 |
| `UserNovelRatingConstraint.java` / `UserNovelRatingValidator.java` | 소설 평점 범위 검증 |

## For AI Agents

### Working In This Directory
- 각 검증 어노테이션은 `*Constraint.java`(어노테이션 정의)와 `*Validator.java`(`ConstraintValidator` 구현) 쌍으로 구성합니다.
- 컨트롤러 요청 DTO의 필드나 파라미터에 어노테이션을 붙여 사용합니다.
- 새 검증 규칙 추가 시 동일한 Constraint + Validator 쌍 패턴을 따릅니다.

### Common Patterns
```java
// Constraint 어노테이션
@Constraint(validatedBy = FooValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FooConstraint {
    String message() default "유효하지 않은 값입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Validator 구현체
public class FooValidator implements ConstraintValidator<FooConstraint, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) { ... }
}
```

<!-- MANUAL: -->
