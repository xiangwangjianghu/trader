package com.newtouch.consumer;

import com.newtouch.dto.order.CommonMsg;
import com.newtouch.transport.checksum.ICheckSum;
import com.newtouch.transport.codec.impl.MsgCodec;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.newtouch.enums.MsgConstants.MATCH_HQ_DATA;
import static com.newtouch.enums.MsgConstants.MATCH_ORDER_DATA;

@Component
@Slf4j
public class MqttBusConsumer {
    /**
     * 行情地址
     */
    private final static String HQ_ADDR = "-1";

    public static final String INNER_MARKET_DATA_CACHE_ADDR = "l1_market_data_cache_addr";

    public static final String INNER_MATCH_DATA_ADDR = "match_data_addr";

    @Value("${engine.id}")
    private short engineId;
    @Value("${counter.id}")
    private String counterId;
    @Value("${engine.bus-ip}")
    private String busIp;
    @Value("${engine.bus-port}")
    private Integer busPort;

    @Autowired
    private ICheckSum checkSum;
    @Autowired
    private MsgCodec msgCodec;

    public void startup(Vertx vertx) {
        MqttClient client = MqttClient.create(vertx);

        client.connect(busPort, busIp, res -> {
            if (res.succeeded()) {
                log.info("成功連接到 mqtt bus[ip:{},port:{}]", busIp, busPort);
                // 訂閱 <地址,次數>
                Map<String, Integer> topic = new HashMap<>() {{
                    put(counterId, MqttQoS.AT_LEAST_ONCE.value());  // 接收地址:櫃檯id
                    put(HQ_ADDR, MqttQoS.AT_LEAST_ONCE.value());    // 行情固定地址
                }};

                client.subscribe(topic, h -> client.publishHandler(m -> {
                    CommonMsg commonMsg = msgCodec.decodeFromBuffer(m.payload());
                    // 校驗數據
                    if (commonMsg.getChecksum() != checkSum.getChecksum(commonMsg.getBody())) {
                        return;
                    }

                    byte[] body = commonMsg.getBody();
                    if (ArrayUtils.isNotEmpty(body)) {
                        switch (commonMsg.getMsgType()) {
                            case MATCH_ORDER_DATA:
                                vertx.eventBus().send(INNER_MATCH_DATA_ADDR, Buffer.buffer(body));
                                break;
                            case MATCH_HQ_DATA:
                                vertx.eventBus().send(INNER_MARKET_DATA_CACHE_ADDR, Buffer.buffer(body));
                                break;
                            default:
                                log.error("無法解析消息類型:{}", commonMsg.getMsgType());
                        }

                    }
                }));
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
}
