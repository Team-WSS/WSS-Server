package org.websoso.support.logging;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.spi.AppenderAttachable;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 로그 출력 경로가 비동기 콘솔 단일 구성으로 유지되는지 검증한다. */
class LogbackConfigurationTest {

    /** 배포 설정 파일을 그대로 읽어 root 로거 구성을 만든다. */
    private Logger configuredRootLogger() throws Exception {
        LoggerContext context = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(getClass().getResourceAsStream("/logback-spring.xml"));
        return context.getLogger(Logger.ROOT_LOGGER_NAME);
    }

    @Test
    @DisplayName("root 로거는 비동기 appender로만 로그를 내보낸다")
    void writesThroughAsyncAppenderOnly() throws Exception {
        List<ch.qos.logback.core.Appender<?>> appenders = new ArrayList<>();
        configuredRootLogger().iteratorForAppenders().forEachRemaining(appenders::add);

        assertThat(appenders).hasSize(1);
        assertThat(appenders.get(0)).isInstanceOf(AsyncAppender.class);
    }

    @Test
    @DisplayName("요청 스레드가 막히지 않도록 큐 포화 시 로그를 버린다")
    void neverBlocksRequestThread() throws Exception {
        AsyncAppender async = (AsyncAppender) configuredRootLogger().getAppender("ASYNC_CONSOLE");

        assertThat(async.isNeverBlock()).isTrue();
        assertThat(async.getQueueSize()).isEqualTo(8192);
        assertThat(async.getDiscardingThreshold()).isZero();
    }

    @Test
    @DisplayName("컨테이너 stdout으로만 수집하므로 파일 appender를 두지 않는다")
    void hasNoFileAppender() throws Exception {
        AppenderAttachable<?> async = (AppenderAttachable<?>) configuredRootLogger().getAppender("ASYNC_CONSOLE");
        List<ch.qos.logback.core.Appender<?>> nested = new ArrayList<>();
        async.iteratorForAppenders().forEachRemaining(nested::add);

        assertThat(nested).noneMatch(FileAppender.class::isInstance);
    }
}
