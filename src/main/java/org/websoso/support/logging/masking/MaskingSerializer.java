package org.websoso.support.logging.masking;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/** 민감정보 필드를 정책에 따라 가린 문자열로 직렬화한다. */
public class MaskingSerializer extends JsonSerializer<Object> {

    private final MaskingPolicy policy;

    public MaskingSerializer(MaskingPolicy policy) {
        this.policy = policy;
    }

    /** 원본 값 대신 마스킹된 문자열을 기록한다. */
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(policy.mask(value));
    }
}
