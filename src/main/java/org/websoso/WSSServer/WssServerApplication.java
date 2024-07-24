package org.websoso.WSSServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WssServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WssServerApplication.class, args);
	}
}
