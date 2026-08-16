package dev.pedrocosta.sentinel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SentinelRiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(SentinelRiskApplication.class, args);
    }
}
