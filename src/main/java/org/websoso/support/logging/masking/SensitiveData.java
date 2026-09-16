package org.websoso.support.logging.masking;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 로그에 남길 때 마스킹할 민감정보 필드를 표시한다.
 * <p>
 * 개인정보처리방침상 수집 항목과 인증 크리덴셜에 부착한다. 이 어노테이션은 로그 전용 직렬화에만
 * 적용되므로 실제 API 응답 본문은 변경되지 않는다. record 컴포넌트에 붙이면 필드·접근자·생성자
 * 파라미터에 함께 전파되어 Jackson이 인식한다.
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SensitiveData {

    /** 적용할 마스킹 규칙이다. */
    MaskingPolicy value() default MaskingPolicy.FULL;
}
