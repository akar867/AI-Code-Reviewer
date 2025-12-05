package com.aicode.reviewer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public WebClient githubWebClient(GithubProperties properties) {
        HttpClient httpClient = HttpClient.create();
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(properties.getApiUrl())
                .defaultHeader("Accept", "application/vnd.github+json")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(4 * 1024 * 1024))
                        .build());

        if (properties.getToken() != null && !properties.getToken().isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + properties.getToken());
        }

        return builder.build();
    }
}
