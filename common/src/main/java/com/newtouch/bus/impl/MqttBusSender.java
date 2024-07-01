package com.newtouch.bus.impl;

import com.newtouch.bus.IBusSender;
import com.newtouch.dto.order.CommonMsg;
import com.newtouch.transport.codec.IMsgCodec;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MqttBusSender implements IBusSender {
    @Autowired
    private IMsgCodec msgCodec;

    @Value("${engine.bus-ip}")
    private String busIp;

    @Value("${engine.bus-port}")
    private Integer busPort;

    private volatile MqttClient sender;

    @Override
    public void startup(Vertx vertx) {
        MqttClient client = MqttClient.create(vertx);

        client.connect(busPort, busIp, res -> {
            if (res.succeeded()) {
                log.info("成功連接到 mqtt bus[ip:{},port:{}]", busIp, busPort);
                sender = client;
            } else {
                log.info("連接mqtt bus[ip:{},port:{}] 失敗", busIp, busPort);
                //重连
                startup(vertx);
            }
        });

        client.closeHandler(s -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                log.error(String.valueOf(e));
            } finally {
                startup(vertx);
            }
        });
    }

    @Override
    public void publish(CommonMsg commonMsg) {
        sender.publish(String.valueOf(commonMsg.getMsgDst()), msgCodec.encodeToBuffer(commonMsg),
                MqttQoS.AT_LEAST_ONCE, false, false);
    }
}
