package com.newtouch.utils;

import com.newtouch.consumer.MqttBusConsumer;
import io.vertx.core.Vertx;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CounterUtil {

    @Autowired
    private MqttBusConsumer mqttBusConsumer;

    @Autowired
    private TCPUtil tcpUtil;
    private final Vertx vertx = tcpUtil.vertx;

    @PostConstruct
    private void init() {
        mqttBusConsumer.startup(vertx);
    }
}
