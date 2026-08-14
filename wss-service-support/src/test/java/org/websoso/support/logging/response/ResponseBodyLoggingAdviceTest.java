package org.websoso.support.logging.response;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;

/** 응답 본문이 값 없이 구조 요약으로만 기록되는지 검증한다. */
class ResponseBodyLoggingAdviceTest {

    private final ResponseBodyLoggingAdvice advice = new ResponseBodyLoggingAdvice(new ResponseBodySummarizer());
    private final Logger logger = (Logger) LoggerFactory.getLogger(ResponseBodyLoggingAdvice.class);
    private ListAppender<ILoggingEvent> listAppender;

    private record UserInfoResponse(String email, String gender, Integer birth) {
    }

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

    @Test
    @DisplayName("응답 본문을 값 없이 타입과 길이로만 기록한다")
    void logsResponseBodyAsTypeAndLengthOnly() {
        UserInfoResponse body = new UserInfoResponse("reader@websoso.kr", "MALE", 1998);

        Object result = advice.beforeBodyWrite(body, null, null, null, request("/users/my-info"), null);

        assertThat(result).isSameAs(body);
        Map<String, Object> summary = loggedSummary();
        assertThat(summary).containsEntry("type", "UserInfoResponse");
        assertThat(summary.get("fields")).isEqualTo(Map.of(
                "email", "String(len=17)",
                "gender", "String(len=4)",
                "birth", "Integer"
        ));
        assertThat(summary.toString()).doesNotContain("reader@websoso.kr");
    }

    @Test
    @DisplayName("목록 응답은 원소를 순회하지 않고 크기만 기록한다")
    void logsCollectionSizeOnly() {
        List<UserInfoResponse> body = IntStream.range(0, 20)
                .mapToObj(i -> new UserInfoResponse("reader" + i + "@websoso.kr", "MALE", 1998))
                .collect(Collectors.toList());

        advice.beforeBodyWrite(body, null, null, null, request("/feeds"), null);

        Map<String, Object> summary = loggedSummary();
        assertThat(summary).containsEntry("type", "List").containsEntry("size", 20);
        assertThat(summary).containsEntry("elementType", "UserInfoResponse");
        assertThat(summary.toString()).doesNotContain("websoso.kr");
    }

    @Test
    @DisplayName("본문이 없으면 null 타입으로 기록한다")
    void logsNullBody() {
        advice.beforeBodyWrite(null, null, null, null, request("/users/logout"), null);

        assertThat(loggedSummary()).containsEntry("type", "null");
    }

    @Test
    @DisplayName("액추에이터 등 서비스 트래픽이 아닌 경로는 기록하지 않는다")
    void skipsExcludedPaths() {
        advice.beforeBodyWrite(Map.of("status", "UP"), null, null, null, request("/actuator/health"), null);

        assertThat(listAppender.list).isEmpty();
    }

    /** 기록된 요약 본문을 꺼낸다. */
    @SuppressWarnings("unchecked")
    private Map<String, Object> loggedSummary() {
        assertThat(listAppender.list).hasSize(1);
        return (Map<String, Object>) listAppender.list.get(0).getKeyValuePairs().stream()
                .filter(pair -> pair.key.equals("body"))
                .findFirst()
                .orElseThrow()
                .value;
    }

    /** 지정한 경로의 요청을 만든다. */
    private ServerHttpRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setRequestURI(URI.create(path).getPath());
        return new ServletServerHttpRequest(request);
    }
}
