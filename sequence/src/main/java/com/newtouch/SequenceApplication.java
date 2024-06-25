package com.newtouch;

import com.newtouch.configs.SequenceConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = {"com.newtouch.**"})
@EnableAsync
public class SequenceApplication {

    @Autowired
    private SequenceConfig sequenceConfig;

    public static void main(String[] args) {
        SpringApplication.run(SequenceApplication.class, args);
    }

    @PostConstruct
    private void start() {
        for (int i = 0; i < 3; i++) {
            sequenceConfig.startup(i);
        }
    }
}
