package com.newtouch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * <p>启动类<p>
 *
 * @author CC
 * @since 2023/9/20
 **/
@SpringBootApplication
@ComponentScan(basePackages = {"com.newtouch.service"})
public class CounterApplication {

    public static void main(String[] args) {
        SpringApplication.run(CounterApplication.class, args);
    }

}
