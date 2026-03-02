package com.fun.agent.api;

import com.fun.agent.api.config.PlaneProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PlaneProperties.class)
public class FunAiAgentApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FunAiAgentApiApplication.class, args);
    }
}

