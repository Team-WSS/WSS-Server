package org.websoso.support.logging.request;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** 요청과 응답의 구조화 필드 및 traceId 연결을 검증한다. */
class RequestLoggingFilterTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
    private ListAppender<ILoggingEvent> listAppender;

    /** 각 테스트가 기록한 로그만 수집하도록 테스트용 appender를 연결한다. */
    @BeforeEach
    void setUp() {
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    /** 테스트 간 appender와 MDC 상태가 공유되지 않도록 정리한다. */
    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        listAppender.stop();
        MDC.clear();
    }

    /** 인증 요청과 응답이 같은 traceId와 문자열 userId를 사용하는지 검증한다. */
    @Test
    void logsAuthenticatedRequestAndResponseAsStructuredFields() throws Exception {
        RequestLoggingFilter filter = new RequestLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/novels/10");
        request.setQueryString("page=1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            MDC.put(RequestLoggingFilter.USER_ID, "42");
            ((MockHttpServletResponse) servletResponse).setStatus(201);
        });

        List<ILoggingEvent> events = infoEvents();
        assertThat(events).hasSize(2);
        assertThat(keyValues(events.get(0)))
                .containsEntry("event", "API_REQUEST")
                .containsEntry("method", "GET")
                .containsEntry("uri", "/novels/10")
                .containsEntry("query", "page=1")
                .doesNotContainKey("userId");
        assertThat(keyValues(events.get(1)))
                .containsEntry("event", "API_RESPONSE")
                .containsEntry("method", "GET")
                .containsEntry("uri", "/novels/10")
                .containsEntry("status", 201)
                .containsKey("durationMs");

        String requestTraceId = events.get(0).getMDCPropertyMap().get("traceId");
        assertThat(requestTraceId).matches("[0-9a-f]{8}");
        assertThat(events.get(1).getMDCPropertyMap().get("traceId")).isEqualTo(requestTraceId);
        assertThat(events.get(1).getMDCPropertyMap().get("userId")).isEqualTo("42");
        assertThat(MDC.get("traceId")).isNull();
    }

    /** 비로그인 요청의 응답 로그에는 userId 필드가 없는지 검증한다. */
    @Test
    void omitsUserIdForAnonymousResponse() throws Exception {
        RequestLoggingFilter filter = new RequestLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> { });

        assertThat(infoEvents().get(1).getMDCPropertyMap()).doesNotContainKey("userId");
    }

    /** INFO 레벨로 기록된 요청·응답 이벤트만 반환한다. */
    private List<ILoggingEvent> infoEvents() {
        return listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.INFO)
                .toList();
    }

    /** 로그 이벤트의 구조화 키-값 목록을 검증 가능한 Map으로 변환한다. */
    private Map<String, Object> keyValues(ILoggingEvent event) {
        return event.getKeyValuePairs().stream()
                .collect(Collectors.toMap(pair -> pair.key, pair -> pair.value));
    }
}
