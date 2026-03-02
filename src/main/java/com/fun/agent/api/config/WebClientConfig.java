package com.fun.agent.api.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient planeWebClient(WebClient.Builder builder, PlaneProperties planeProperties) {
        return builder
                .baseUrl(planeProperties.baseUrl())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }

    @Bean
    public Duration planeTimeout(PlaneProperties planeProperties) {
        return Duration.ofSeconds(planeProperties.timeoutSeconds());
    }
}
