package com.newtouch.consumer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.newtouch.dto.market.MatchData;
import com.newtouch.dto.order.OrderRequest;
import com.newtouch.enums.OrderStatusEnum;
import com.newtouch.transport.codec.impl.BodyCodec;
import com.newtouch.utils.CounterUtil;
import com.newtouch.utils.IdUtil;
import com.newtouch.utils.TCPUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.newtouch.consumer.MqttBusConsumer.INNER_MATCH_DATA_ADDR;
import static com.newtouch.enums.OrderStatusEnum.CANCEL_ED;
import static com.newtouch.enums.OrderStatusEnum.PART_CANCEL;

@Component
@Slf4j
public class MatchDataConsumer {
    public static final String ORDER_DATA_CACHE_ADDR = "order_data_cache_addr";

    @Autowired
    private WebSocketConsumer webSocketConsumer;
    // <oid, OrderRequest>
    private LongObjectHashMap<OrderRequest> oidOrderMap = new LongObjectHashMap<>();

    @Autowired
    private TCPUtil tcpUtil;
    @Autowired
    private BodyCodec bodyCodec;
    @Autowired
    private IdUtil idUtil;

    private final EventBus eventBus = tcpUtil.vertx.eventBus();

    @PostConstruct
    private void init() {
        // 接收委託終端數據
        eventBus.consumer(ORDER_DATA_CACHE_ADDR)
                .handler(buffer -> {
                    Buffer body = (Buffer) buffer.body();
                    if (body.length() == 0) {
                        return;
                    }

                    try {
                        OrderRequest orderRequest = bodyCodec.deserialize(body.getBytes(), OrderRequest.class);
                        oidOrderMap.put(orderRequest.getId(), orderRequest);
                    } catch (Exception e) {
                        log.error("無法解析訂單請求: ", e);
                    }
                });

        // 接收總線撮合數據
        eventBus.consumer(INNER_MATCH_DATA_ADDR)
                .handler(buffer -> {
                    Buffer body = (Buffer) buffer.body();
                    if (body.length() == 0) {
                        return;
                    }

                    MatchData[] matchDataArr = null;
                    try {
                        matchDataArr = bodyCodec.deserialize(body.getBytes(), MatchData[].class);
                    } catch (Exception e) {
                        log.error("無法解析訂單請求: ", e);
                    }

                    if (ArrayUtils.isEmpty(matchDataArr)) {
                        return;
                    }
                    // <oid, List<MatchData>>
                    Map<Long, List<MatchData>> collect = Arrays.stream(matchDataArr).collect(Collectors.groupingBy(MatchData::getOid));
                    for (Map.Entry<Long, List<MatchData>> entry : collect.entrySet()) {
                        if (CollectionUtils.isNotEmpty(entry.getValue())) {
                            Long oid = entry.getKey();
                            Long counterOid = idUtil.separateOid(oid)[1];

                            updateAndNotify(counterOid, entry.getValue(), oidOrderMap.get(oid));
                        }
                    }
                });
    }

    private void updateAndNotify(Long counterOid, List<MatchData> value, OrderRequest orderRequest) {
        // 1.成交變動
        for (MatchData matchData : value) {
            OrderStatusEnum status = matchData.getStatus();

            if (status == OrderStatusEnum.TRADE_ED || status == OrderStatusEnum.PART_TRADE) {
                // 數據庫操作,省略

                // 通知客戶端撮合變動
                eventBus.publish(webSocketConsumer.TRADE_NOTIFY_ADDR_PREFIX + orderRequest.getUid(), JSON.toJSON(ImmutableMap.of("code", orderRequest.getCode(),
                        "direction", orderRequest.getDirection(),
                        "volume", matchData.getCount())));
            }
        }

        // 2.委託變動
        MatchData lastMatchData = value.get(value.size() - 1);
        OrderStatusEnum status = lastMatchData.getStatus();
        if (status == CANCEL_ED || status == PART_CANCEL) {
            oidOrderMap.remove(orderRequest.getCounterOid());

            // 數據庫操作,省略
        }

        // 通知委託終端撮合變動
        eventBus.publish(webSocketConsumer.ORDER_NOTIFY_ADDR_PREFIX + orderRequest.getUid(), "");
    }
}
