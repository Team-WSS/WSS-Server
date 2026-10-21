package org.websoso.support.logging.masking;

import java.util.List;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;

/** 민감정보로 표시된 프로퍼티의 직렬화기를 마스킹 직렬화기로 교체한다. */
public class MaskingSerializerModifier extends ValueSerializerModifier {

    /** 로그 전용 ObjectMapper에서만 호출되므로 실제 API 응답 직렬화에는 영향이 없다. */
    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription.Supplier beanDescription,
                                                     List<BeanPropertyWriter> beanProperties) {
        for (BeanPropertyWriter writer : beanProperties) {
            MaskingPolicy policy = resolvePolicy(writer);
            if (policy != null) {
                writer.assignSerializer(new MaskingSerializer(policy));
            }
        }
        return beanProperties;
    }

    /** 어노테이션을 우선 적용하고, 없으면 필드 이름 안전망으로 판단한다. */
    private MaskingPolicy resolvePolicy(BeanPropertyWriter writer) {
        SensitiveData annotation = writer.getAnnotation(SensitiveData.class);
        if (annotation != null) {
            return annotation.value();
        }
        return SensitiveFieldNames.policyOf(writer.getName());
    }
}
