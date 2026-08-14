package org.websoso.support.logging.masking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 본문을 민감정보가 가려진 JSON으로 변환한다.
 * <p>
 * 마스킹 직렬화기는 이 클래스가 직접 만든 전용 ObjectMapper에만 등록한다. ObjectMapper 타입 빈을
 * 노출하면 Spring Boot 기본 ObjectMapper 자동 구성을 밀어내 실제 API 응답 직렬화까지 바뀌므로,
 * 매퍼를 감싼 컴포넌트 형태로만 제공한다.
 */
@Component
public class SensitiveDataMasker {

    /** 로그 한 건이 지나치게 커지지 않도록 제한하는 직렬화 결과 길이다. */
    private static final int MAX_BODY_LENGTH = 4096;
    private static final String TRUNCATED_SUFFIX = "...(truncated)";

    private final ObjectMapper maskingMapper;

    public SensitiveDataMasker() {
        SimpleModule maskingModule = new SimpleModule();
        maskingModule.setSerializerModifier(new MaskingSerializerModifier());
        this.maskingMapper = JsonMapper.builder()
                .findAndAddModules()
                .addModule(maskingModule)
                .build();
    }

    /** 민감정보를 가린 JSON으로 바꾸고, 상한을 넘으면 잘라낸 문자열로 대체한다. */
    public MaskedBody mask(Object body) {
        JsonNode masked = maskingMapper.valueToTree(body);
        maskByFieldName(masked);
        String serialized = masked.toString();
        if (serialized.length() <= MAX_BODY_LENGTH) {
            return new MaskedBody(masked, false);
        }
        return new MaskedBody(TextNode.valueOf(serialized.substring(0, MAX_BODY_LENGTH) + TRUNCATED_SUFFIX), true);
    }

    /**
     * 필드 이름 안전망을 JSON 트리 전체에 적용한다.
     * <p>
     * 어노테이션 기반 마스킹은 record·POJO 프로퍼티에만 걸리므로, Map 본문이나 중첩 구조는
     * 직렬화 결과를 순회하며 한 번 더 걸러야 평문 유출을 막을 수 있다.
     */
    private void maskByFieldName(JsonNode node) {
        if (node.isArray()) {
            node.forEach(this::maskByFieldName);
            return;
        }
        if (!node.isObject()) {
            return;
        }
        ObjectNode objectNode = (ObjectNode) node;
        List<String> fieldNames = new ArrayList<>();
        objectNode.fieldNames().forEachRemaining(fieldNames::add);
        for (String fieldName : fieldNames) {
            JsonNode value = objectNode.get(fieldName);
            MaskingPolicy policy = SensitiveFieldNames.policyOf(fieldName);
            if (policy == null || !value.isValueNode() || value.isNull()) {
                maskByFieldName(value);
                continue;
            }
            if (!MaskingPolicy.isMasked(value.asText())) {
                objectNode.put(fieldName, policy.mask(value.asText()));
            }
        }
    }
}
