package org.websoso.support.logging.response;

import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 응답 본문을 값 없이 구조만 담은 요약으로 변환한다.
 * <p>
 * 값을 직렬화하지 않으므로 개인정보가 로그로 흘러갈 수 없고, 목록 응답도 원소를 순회하지 않아
 * 크기와 무관하게 비용이 일정하다.
 */
public class ResponseBodySummarizer {

    private static final Map<Class<?>, RecordComponent[]> RECORD_COMPONENTS = new ConcurrentHashMap<>();
    private static final String UNREADABLE = "unreadable";

    /** 응답 본문의 타입과 필드 구성을 요약한다. */
    public Map<String, Object> summarize(Object body) {
        if (body == null) {
            return Map.of("type", "null");
        }
        if (body instanceof Collection<?> collection) {
            return summarizeCollection(collection);
        }
        if (body instanceof Map<?, ?> map) {
            return Map.of("type", "Map", "size", map.size());
        }
        Class<?> type = body.getClass();
        if (type.isRecord()) {
            return summarizeRecord(body, type);
        }
        if (body instanceof CharSequence text) {
            return Map.of("type", "String", "length", text.length());
        }
        return Map.of("type", type.getSimpleName());
    }

    /** 목록은 원소를 순회하지 않고 크기와 원소 타입만 남긴다. */
    private Map<String, Object> summarizeCollection(Collection<?> collection) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("type", "List");
        summary.put("size", collection.size());
        collection.stream()
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .ifPresent(element -> summary.put("elementType", element.getClass().getSimpleName()));
        return summary;
    }

    /** record는 필드명과 값의 형태만 남기고 값 자체는 기록하지 않는다. */
    private Map<String, Object> summarizeRecord(Object body, Class<?> type) {
        Map<String, Object> fields = new LinkedHashMap<>();
        for (RecordComponent component : RECORD_COMPONENTS.computeIfAbsent(type, Class::getRecordComponents)) {
            fields.put(component.getName(), describe(body, component));
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("type", type.getSimpleName());
        summary.put("fields", fields);
        return summary;
    }

    /** 값 자체는 남기지 않고 타입과 길이만 표현한다. */
    private String describe(Object body, RecordComponent component) {
        Object value;
        try {
            value = component.getAccessor().invoke(body);
        } catch (Exception e) {
            return UNREADABLE;
        }
        if (value == null) {
            return component.getType().getSimpleName() + "(null)";
        }
        if (value instanceof CharSequence text) {
            return "String(len=" + text.length() + ")";
        }
        if (value instanceof Collection<?> collection) {
            return "List(size=" + collection.size() + ")";
        }
        if (value instanceof Map<?, ?> map) {
            return "Map(size=" + map.size() + ")";
        }
        return value.getClass().getSimpleName();
    }
}
