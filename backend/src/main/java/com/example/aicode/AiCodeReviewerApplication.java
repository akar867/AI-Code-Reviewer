package com.example.aicode;

import com.example.aicode.config.AiClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AiClientProperties.class)
public class AiCodeReviewerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCodeReviewerApplication.class, args);
    }
}
