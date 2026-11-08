package org.websoso.support.logging.masking;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/** 민감정보 필드를 정책에 따라 가린 문자열로 직렬화한다. */
public class MaskingSerializer extends ValueSerializer<Object> {

    private final MaskingPolicy policy;

    public MaskingSerializer(MaskingPolicy policy) {
        this.policy = policy;
    }

    /** 원본 값 대신 마스킹된 문자열을 기록한다. */
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext serializers) {
        gen.writeString(policy.mask(value));
    }
}
