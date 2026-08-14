package org.websoso.support.logging.request;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpInputMessage;

/** 요청 본문이 구조화 로그의 body 필드에 유지되는지 검증한다. */
class RequestBodyLoggingAdviceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = (Logger) LoggerFactory.getLogger(RequestBodyLoggingAdvice.class);
    private ListAppender<ILoggingEvent> listAppender;

    /** 각 테스트가 기록한 로그만 수집하도록 테스트용 appender를 연결한다. */
    @BeforeEach
    void setUp() {
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    /** 테스트가 사용한 appender를 로거에서 제거한다. */
    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        listAppender.stop();
        MDC.clear();
    }

    /** 요청 본문을 누락하지 않고 JSON 객체 필드로 기록하는지 검증한다. */
    @Test
    void logsRequestBodyAsStructuredJsonField() {
        RequestBodyLoggingAdvice advice = new RequestBodyLoggingAdvice();
        Map<String, String> body = Map.of("content", "first\nsecond");
        MDC.put("traceId", "trace-1234");
        MDC.put(RequestLoggingFilter.USER_ID, "42");

        Object result = advice.afterBodyRead(
                body,
                new MockHttpInputMessage(new byte[0]),
                null,
                Map.class,
                MappingJackson2HttpMessageConverter.class
        );

        assertThat(result).isSameAs(body);
        assertThat(listAppender.list).hasSize(1);
        Map<String, Object> fields = keyValues(listAppender.list.get(0));
        assertThat(fields).containsEntry("event", "API_REQUEST_BODY");
        assertThat(fields.get("body"))
                .isEqualTo(objectMapper.<JsonNode>valueToTree(body));
        assertThat(listAppender.list.get(0).getMDCPropertyMap())
                .containsEntry("traceId", "trace-1234")
                .containsEntry("userId", "42");
    }

    /** 요청 본문의 민감정보를 가린 채 기록하는지 검증한다. */
    @Test
    void masksSensitiveFieldsInRequestBody() {
        RequestBodyLoggingAdvice advice = new RequestBodyLoggingAdvice();
        Map<String, String> body = Map.of("refreshToken", "eyJhbGciOiJIUzI1NiJ9", "content", "일반 내용");

        advice.afterBodyRead(
                body,
                new MockHttpInputMessage(new byte[0]),
                null,
                Map.class,
                MappingJackson2HttpMessageConverter.class
        );

        Map<String, Object> fields = keyValues(listAppender.list.get(0));
        JsonNode logged = (JsonNode) fields.get("body");
        assertThat(logged.get("refreshToken").asText()).isEqualTo("***(len=20)");
        assertThat(logged.get("content").asText()).isEqualTo("일반 내용");
        assertThat(fields).containsEntry("truncated", false);
    }

    /** 로그 이벤트의 구조화 키-값 목록을 검증 가능한 Map으로 변환한다. */
    private Map<String, Object> keyValues(ILoggingEvent event) {
        return event.getKeyValuePairs().stream()
                .collect(Collectors.toMap(pair -> pair.key, pair -> pair.value));
    }
}
