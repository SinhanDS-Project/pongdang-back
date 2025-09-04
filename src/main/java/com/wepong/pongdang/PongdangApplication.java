package com.wepong.pongdang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication(scanBasePackages = {
        "com.wepong.pongdang",    // 퐁당
        "net.wepong.mysql"        // BettingPoint DB
})
public class PongdangApplication {

	public static void main(String[] args) {
		SpringApplication.run(PongdangApplication.class, args);
	}

}
