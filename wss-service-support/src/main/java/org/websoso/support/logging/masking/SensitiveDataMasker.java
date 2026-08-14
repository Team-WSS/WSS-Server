package org.websoso.support.logging.masking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.TextNode;
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
        String serialized = masked.toString();
        if (serialized.length() <= MAX_BODY_LENGTH) {
            return new MaskedBody(masked, false);
        }
        return new MaskedBody(TextNode.valueOf(serialized.substring(0, MAX_BODY_LENGTH) + TRUNCATED_SUFFIX), true);
    }
}
