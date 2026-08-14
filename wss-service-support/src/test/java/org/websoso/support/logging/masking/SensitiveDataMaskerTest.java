package org.websoso.support.logging.masking;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 민감정보 마스킹 결과와 실제 응답 직렬화 비침해를 검증한다. */
class SensitiveDataMaskerTest {

    private final SensitiveDataMasker masker = new SensitiveDataMasker();

    private record LoginRequest(
            @SensitiveData(MaskingPolicy.CREDENTIAL) String refreshToken,
            @SensitiveData(MaskingPolicy.EMAIL) String email,
            @SensitiveData(MaskingPolicy.NAME) String nickname,
            @SensitiveData(MaskingPolicy.FULL) Integer birth,
            String novelTitle
    ) {
    }

    private record UnannotatedRequest(
            String refreshToken,
            String email,
            String novelTitle
    ) {
    }

    @Test
    @DisplayName("어노테이션이 붙은 필드를 정책에 맞게 가린다")
    void masksAnnotatedFieldsByPolicy() {
        LoginRequest body = new LoginRequest("eyJhbGciOiJIUzI1NiJ9.signature", "reader@websoso.kr", "웹소소독자", 1998,
                "전지적 독자 시점");

        JsonNode masked = masker.mask(body).json();

        assertThat(masked.get("refreshToken").asText()).isEqualTo("***(len=30)");
        assertThat(masked.get("email").asText()).isEqualTo("re***@we***");
        assertThat(masked.get("nickname").asText()).isEqualTo("웹***");
        assertThat(masked.get("birth").asText()).isEqualTo("***");
    }

    @Test
    @DisplayName("민감정보가 아닌 필드는 값을 그대로 남긴다")
    void keepsNonSensitiveFieldValues() {
        LoginRequest body = new LoginRequest("token", "reader@websoso.kr", "웹소소독자", 1998, "전지적 독자 시점");

        JsonNode masked = masker.mask(body).json();

        assertThat(masked.get("novelTitle").asText()).isEqualTo("전지적 독자 시점");
    }

    @Test
    @DisplayName("어노테이션이 없어도 민감한 필드명은 안전망으로 가린다")
    void masksKnownSensitiveFieldNamesWithoutAnnotation() {
        UnannotatedRequest body = new UnannotatedRequest("eyJhbGciOiJIUzI1NiJ9", "reader@websoso.kr", "전지적 독자 시점");

        JsonNode masked = masker.mask(body).json();

        assertThat(masked.get("refreshToken").asText()).isEqualTo("***(len=20)");
        assertThat(masked.get("email").asText()).isEqualTo("re***@we***");
        assertThat(masked.get("novelTitle").asText()).isEqualTo("전지적 독자 시점");
    }

    @Test
    @DisplayName("마스킹 설정이 실제 응답 직렬화에는 영향을 주지 않는다")
    void doesNotAffectApiResponseSerialization() {
        LoginRequest body = new LoginRequest("eyJhbGciOiJIUzI1NiJ9", "reader@websoso.kr", "웹소소독자", 1998, "전지적 독자 시점");

        JsonNode apiResponse = new ObjectMapper().valueToTree(body);

        assertThat(apiResponse.get("refreshToken").asText()).isEqualTo("eyJhbGciOiJIUzI1NiJ9");
        assertThat(apiResponse.get("email").asText()).isEqualTo("reader@websoso.kr");
        assertThat(apiResponse.get("nickname").asText()).isEqualTo("웹소소독자");
        assertThat(apiResponse.get("birth").asInt()).isEqualTo(1998);
    }

    @Test
    @DisplayName("Map 본문과 중첩 구조에도 필드명 안전망을 적용한다")
    void masksSensitiveFieldNamesInMapAndNestedNodes() {
        Map<String, Object> body = Map.of(
                "email", "reader@websoso.kr",
                "device", Map.of("fcmToken", "fcm-token-value", "model", "iPhone 15"),
                "novelTitle", "전지적 독자 시점"
        );

        JsonNode masked = masker.mask(body).json();

        assertThat(masked.get("email").asText()).isEqualTo("re***@we***");
        assertThat(masked.get("device").get("fcmToken").asText()).isEqualTo("***(len=15)");
        assertThat(masked.get("device").get("model").asText()).isEqualTo("iPhone 15");
        assertThat(masked.get("novelTitle").asText()).isEqualTo("전지적 독자 시점");
    }

    @Test
    @DisplayName("어노테이션으로 가린 값을 안전망이 다시 가리지 않는다")
    void doesNotMaskAlreadyMaskedValueTwice() {
        LoginRequest body = new LoginRequest("0123456789", "reader@websoso.kr", "웹소소독자", 1998, "전지적 독자 시점");

        JsonNode masked = masker.mask(body).json();

        assertThat(masked.get("refreshToken").asText()).isEqualTo("***(len=10)");
    }

    @Test
    @DisplayName("상한을 넘는 본문은 잘라내고 truncated로 표시한다")
    void truncatesOversizedBody() {
        List<String> body = List.of("가".repeat(3000), "나".repeat(3000));

        MaskedBody masked = masker.mask(body);

        assertThat(masked.truncated()).isTrue();
        assertThat(masked.json().asText()).endsWith("...(truncated)");
    }

    @Test
    @DisplayName("상한 이하 본문은 JSON 구조를 유지한다")
    void keepsJsonStructureWithinLimit() {
        MaskedBody masked = masker.mask(new UnannotatedRequest("token", "reader@websoso.kr", "전지적 독자 시점"));

        assertThat(masked.truncated()).isFalse();
        assertThat(masked.json().isObject()).isTrue();
    }
}
