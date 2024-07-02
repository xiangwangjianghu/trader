package com.newtouch.consumer;

import com.alibaba.fastjson2.JSON;
import com.newtouch.dto.market.L1MarketData;
import com.newtouch.dto.market.MatchData;
import com.newtouch.dto.order.OrderRequest;
import com.newtouch.transport.codec.impl.BodyCodec;
import com.newtouch.utils.IdUtil;
import com.newtouch.utils.TCPUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.newtouch.consumer.MqttBusConsumer.INNER_MARKET_DATA_CACHE_ADDR;
import static com.newtouch.consumer.MqttBusConsumer.INNER_MATCH_DATA_ADDR;
import static com.newtouch.consumer.WebSocketConsumer.L1_MARKET_DATA_PREFIX;

@Component
@Slf4j
public class MarketDataConsumer {
    //<key code,value 最新的五档行情 >
    private IntObjectHashMap<L1MarketData> l1Cache = new IntObjectHashMap<>();

    @Autowired
    private BodyCodec bodyCodec;
    @Autowired
    private IdUtil idUtil;
    @Autowired
    private TCPUtil tcpUtil;
    private final EventBus eventBus = tcpUtil.vertx.eventBus();

    @PostConstruct
    private void init() {
        // 接收撮合行情數據(用于更新最新五档行情)
        eventBus.consumer(INNER_MARKET_DATA_CACHE_ADDR)
                .handler(buffer -> {
                    Buffer body = (Buffer) buffer.body();
                    if (body.length() == 0) {
                        return;
                    }

                    L1MarketData[] marketData = null;
                    try {
                        marketData = bodyCodec.deserialize(body.getBytes(), L1MarketData[].class);
                    } catch (Exception e) {
                        log.error("無法解析訂單請求: ", e);
                    }

                    if (ArrayUtils.isNotEmpty(marketData)) {
                        for (L1MarketData l1MarketData : marketData) {
                            L1MarketData data = l1Cache.get(l1MarketData.getCode());
                            if (data == null || data.timestamp < l1MarketData.timestamp) {
                                l1Cache.put(l1MarketData.getCode(), l1MarketData);
                            } else {
                                log.error("l1MarketData is null or l1MarketData.timestamp < md.timestamp");
                            }
                        }
                    }
                });

        //委托终端行情处理器 接受五档行情请求 数据来自 WebSocketConfig 接受的 L1_MARKET_DATA_PREFIX
        //用于回复五档行情求情
        eventBus.consumer(L1_MARKET_DATA_PREFIX)
                .handler(buffer -> {
                    int code = Integer.parseInt(buffer.headers().get("code"));
                    L1MarketData l1MarketData = l1Cache.get(code);
                    // 回復行情數據
                    buffer.reply(JSON.toJSONString(l1MarketData));
                });
    }

}
