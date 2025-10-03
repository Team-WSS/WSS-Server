package org.websoso.WSSServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
@EntityScan(basePackages = {"org.websoso.WSSServer", "org.websoso.support"})
@EnableJpaRepositories(basePackages = {"org.websoso.WSSServer", "org.websoso.support"})
@SpringBootApplication(scanBasePackages = {"org.websoso.WSSServer", "org.websoso.support", "org.websoso.common"})
public class WssServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WssServerApplication.class, args);
    }
}
