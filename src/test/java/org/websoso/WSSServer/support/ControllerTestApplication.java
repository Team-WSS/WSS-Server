package org.websoso.WSSServer.support;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

/**
 * Controller 슬라이스 테스트 전용 구성 진입점.
 * 프로덕션 진입점({@code WssServerApplication})은 JPA/Redis Repository 등록과 스케줄링을 함께 켜기 때문에
 * 웹 계층 테스트에서도 Redis 인프라 빈을 요구한다. 이 클래스는 컴포넌트 스캔 범위만 동일하게 유지하고
 * 외부 연동(JPA, Redis, 스케줄링) 활성화는 제외해, 슬라이스 테스트가 실제 Redis/DB 없이 뜨게 한다.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
        basePackages = {"org.websoso.WSSServer", "org.websoso.support", "org.websoso.common"},
        excludeFilters = {
                @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
                @Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class)
        })
public class ControllerTestApplication {
}
