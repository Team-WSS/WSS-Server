package org.websoso.support.logging.masking;

import java.util.Locale;
import java.util.Map;

/**
 * 어노테이션 부착이 누락된 필드를 이름으로 걸러내는 안전망이다.
 * <p>
 * 신규 DTO에 {@link SensitiveData}를 빠뜨려도 평문 유출을 막는 것이 목적이므로, 오탐 없이
 * 민감정보가 확실한 이름만 등록한다. 닉네임·이름처럼 다른 도메인 용어와 겹치는 이름은
 * 작품명·장르명까지 가려버리므로 넣지 않고 어노테이션으로만 처리한다.
 */
final class SensitiveFieldNames {

    private static final Map<String, MaskingPolicy> POLICIES = Map.ofEntries(
            Map.entry("email", MaskingPolicy.EMAIL),
            Map.entry("password", MaskingPolicy.CREDENTIAL),
            Map.entry("authorization", MaskingPolicy.CREDENTIAL),
            Map.entry("accesstoken", MaskingPolicy.CREDENTIAL),
            Map.entry("access_token", MaskingPolicy.CREDENTIAL),
            Map.entry("refreshtoken", MaskingPolicy.CREDENTIAL),
            Map.entry("refresh_token", MaskingPolicy.CREDENTIAL),
            Map.entry("idtoken", MaskingPolicy.CREDENTIAL),
            Map.entry("id_token", MaskingPolicy.CREDENTIAL),
            Map.entry("authorizationcode", MaskingPolicy.CREDENTIAL),
            Map.entry("fcmtoken", MaskingPolicy.CREDENTIAL),
            Map.entry("deviceidentifier", MaskingPolicy.CREDENTIAL),
            Map.entry("useridentifier", MaskingPolicy.CREDENTIAL),
            Map.entry("socialid", MaskingPolicy.CREDENTIAL),
            Map.entry("phone", MaskingPolicy.FULL),
            Map.entry("phonenumber", MaskingPolicy.FULL),
            Map.entry("address", MaskingPolicy.FULL)
    );

    private SensitiveFieldNames() {
    }

    /** 필드 이름만으로 적용할 기본 마스킹 규칙을 찾는다. 대상이 아니면 null을 반환한다. */
    static MaskingPolicy policyOf(String propertyName) {
        return POLICIES.get(propertyName.toLowerCase(Locale.ROOT));
    }
}
