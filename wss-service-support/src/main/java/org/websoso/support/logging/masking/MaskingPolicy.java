package org.websoso.support.logging.masking;

/** 민감정보 종류별 로그 마스킹 규칙을 정의한다. */
public enum MaskingPolicy {

    /** 이메일 주소를 계정·도메인 앞 2자만 남기고 가린다. */
    EMAIL {
        @Override
        public String mask(Object value) {
            String raw = String.valueOf(value);
            int separator = raw.indexOf('@');
            if (separator <= 0) {
                return MASK;
            }
            return head(raw.substring(0, separator)) + MASK + "@" + head(raw.substring(separator + 1)) + MASK;
        }
    },

    /** 이름·닉네임을 첫 글자만 남기고 가린다. */
    NAME {
        @Override
        public String mask(Object value) {
            String raw = String.valueOf(value);
            return raw.isEmpty() ? MASK : raw.substring(0, 1) + MASK;
        }
    },

    /** 토큰·인증코드·기기 식별자는 값을 전부 가리고 길이만 남겨 장애 판별에 활용한다. */
    CREDENTIAL {
        @Override
        public String mask(Object value) {
            return MASK + "(len=" + String.valueOf(value).length() + ")";
        }
    },

    /** 성별·출생연도처럼 값 자체가 식별 정보인 항목을 전부 가린다. */
    FULL {
        @Override
        public String mask(Object value) {
            return MASK;
        }
    };

    private static final String MASK = "***";
    private static final int VISIBLE_LENGTH = 2;

    /** 원본 값을 정책에 맞게 가린 문자열로 변환한다. */
    public abstract String mask(Object value);

    /** 마스킹 시 노출을 허용하는 앞부분만 잘라낸다. */
    private static String head(String value) {
        return value.length() <= VISIBLE_LENGTH ? value : value.substring(0, VISIBLE_LENGTH);
    }
}
