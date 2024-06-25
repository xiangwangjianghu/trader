package com.newtouch;

import com.newtouch.configs.EngineConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = {"com.newtouch.**"})
@EnableAsync
public class EngineApplication {

    @Autowired
    private EngineConfig engineConfig;

    public static void main(String[] args) {
        SpringApplication.run(EngineApplication.class, args);
    }

    @PostConstruct
    private void start() {
        for (int i = 0; i < 3; i++) {
            engineConfig.startup(i);
        }
    }
}
