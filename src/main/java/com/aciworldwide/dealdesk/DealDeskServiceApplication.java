package com.aciworldwide.dealdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import com.aciworldwide.dealdesk.rules.config.RuleEngineProperties;

@SpringBootApplication
@EnableMongoAuditing
@EnableCaching
@EnableAsync
@EnableConfigurationProperties(RuleEngineProperties.class)
public class DealDeskServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DealDeskServiceApplication.class, args);
    }
}