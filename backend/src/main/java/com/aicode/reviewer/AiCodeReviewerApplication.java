package com.aicode.reviewer;

import com.aicode.reviewer.config.GithubProperties;
import com.aicode.reviewer.config.LlmProperties;
import com.aicode.reviewer.config.ReviewerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({GithubProperties.class, LlmProperties.class, ReviewerProperties.class})
public class AiCodeReviewerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCodeReviewerApplication.class, args);
    }
}
