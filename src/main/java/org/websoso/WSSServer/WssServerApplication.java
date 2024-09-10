package org.websoso.WSSServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableRedisRepositories
@SpringBootApplication
public class WssServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WssServerApplication.class, args);
	}
}
