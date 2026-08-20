package org.websoso.support.logging.masking;

import com.fasterxml.jackson.databind.JsonNode;

/** 마스킹된 본문과 크기 상한 초과로 잘렸는지 여부를 함께 전달한다. */
public record MaskedBody(
        JsonNode json,
        boolean truncated
) {
}
